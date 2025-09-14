package me.notej.notej_api.security.refresh.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.notej.notej_api.core.member.domain.Member;
import me.notej.notej_api.security.jwt.TokenProvider;
import me.notej.notej_api.security.refresh.domain.RefreshToken;
import me.notej.notej_api.security.refresh.dto.AccessTokenRefreshResponse;
import me.notej.notej_api.security.refresh.exception.InvalidRefreshTokenException;
import me.notej.notej_api.security.refresh.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccessTokenRefreshServiceV0 implements AccessTokenRefreshService {

    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    @Transactional
    public AccessTokenRefreshResponse refresh(String grantType, String oldRefresh) {
        log.info("[TokenRefresh] 요청 시작 - grant_type: {}, oldRefresh: {}", grantType, oldRefresh);

        // 1) grant_type 검증
        if (!"refresh_token".equals(grantType)) {
            String errorMessage = "unsupported_grant_type: " + grantType;
            log.error("[TokenRefresh] 잘못된 grant_type: {}", grantType);
            throw new InvalidRefreshTokenException(errorMessage);
        }

        // 2) 리프레시 토큰 무결성·만료 검증
        if (!tokenProvider.validateRefreshToken(oldRefresh)) {
            log.error("[TokenRefresh] 리프레시 토큰 유효하지 않거나 만료됨: {}", oldRefresh);
            throw new InvalidRefreshTokenException("invalid_or_expired_refresh_token");
        }

        // 3) DB 조회 및 폐기 상태 확인
        RefreshToken stored = refreshTokenRepository.findByToken(oldRefresh)
                .orElseThrow(() -> {
                    log.error("[TokenRefresh] DB에 존재하지 않는 리프레시 토큰: {}", oldRefresh);
                    return new InvalidRefreshTokenException("Refresh_token_not_found_in_DB");
                });

        if (stored.isRevoked()) {
            log.error("[TokenRefresh] 이미 폐기된 리프레시 토큰: {}", oldRefresh);
            throw new InvalidRefreshTokenException("Refresh_token_revoked");
        }

        Member member = stored.getMember();
        log.info("[TokenRefresh] 유효한 리프레시 토큰 확인 완료 - memberId: {}, uuid: {}",
                member.getId(), member.getMemberUuid());

        // 4) 새 토큰 발급
        String newAccess  = tokenProvider.createToken(member.getMemberUuid());
        String newRefresh = tokenProvider.createRefreshToken(member.getMemberUuid());
        long   expiresIn  = tokenProvider.getAccessTokenExpiryMs() / 1000;

        log.info("[TokenRefresh] 새 액세스/리프레시 토큰 생성 완료");

        // 5) 기존 토큰 폐기 & 새 토큰 저장
        stored.revoke();
        refreshTokenRepository.save(
                RefreshToken.builder()
                        .member(stored.getMember())
                        .token(newRefresh)
                        .expiryDate(Instant.now().plusMillis(tokenProvider.getRefreshTokenExpiryMs()))
                        .revoked(false)
                        .build()
        );

        log.info("[TokenRefresh] 기존 리프레시 토큰 폐기 및 신규 토큰 저장 완료");

        // 6) 응답 반환
        return new AccessTokenRefreshResponse(newAccess, newRefresh, expiresIn);
    }

}
