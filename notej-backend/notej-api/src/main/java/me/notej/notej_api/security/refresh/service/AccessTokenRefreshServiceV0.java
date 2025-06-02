package me.notej.notej_api.security.refresh.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.notej.notej_api.security.domain.Member;
import me.notej.notej_api.security.jwt.TokenProvider;
import me.notej.notej_api.security.refresh.domain.RefreshToken;
import me.notej.notej_api.security.refresh.dto.AccessTokenRefreshResponse;
import me.notej.notej_api.security.refresh.exception.InvalidRefreshTokenException;
import me.notej.notej_api.security.repository.RefreshTokenRepository;
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
        // 1) grant_type 검증
        if (!"refresh_token".equals(grantType)) {
            String errorMessage = "unsupported_grant_type: " + grantType;
            log.error("Invalid_refresh_token_request: {}", errorMessage);
            throw new InvalidRefreshTokenException(errorMessage);
        }

        // 2) 리프레시 토큰 무결성·만료 검증
        if (!tokenProvider.validateRefreshToken(oldRefresh)) {
            String errorMessage = "invalid_or_expired_refresh_token: " + oldRefresh;
            throw new InvalidRefreshTokenException(errorMessage);
        }

        // 3) DB 조회 및 폐기 상태 확인
        RefreshToken stored = refreshTokenRepository.findByToken(oldRefresh)
                .orElseThrow(() -> {
                    log.error("Refresh_token_not_found_in_DB: {}", oldRefresh);
                    return new InvalidRefreshTokenException("Refresh_token_not_found_in_DB");
                });

        // 폐기 상태 확인
        if (stored.isRevoked()) {
            log.error("Refresh_token_revoked: {}", oldRefresh);
            throw new InvalidRefreshTokenException("Refresh_token_revoked");
        }

        // 새 토큰 발급
        Member member = stored.getMember();

        String newAccess  = tokenProvider.createToken(member.getMemberUuid());
        String newRefresh = tokenProvider.createRefreshToken(member.getMemberUuid());
        long   expiresIn  = tokenProvider.getAccessTokenExpiryMs() / 1000;

        // 이전 리프레시 토큰 폐기 & 신규 저장
        stored.revoke();

        refreshTokenRepository.save(
                RefreshToken.builder()
                        .member(stored.getMember())
                        .token(newRefresh)
                        .expiryDate(Instant.now().plusMillis(tokenProvider.getRefreshTokenExpiryMs()))
                        .revoked(false)
                        .build()
        );

        return new AccessTokenRefreshResponse(newAccess, newRefresh, expiresIn);
    }
}
