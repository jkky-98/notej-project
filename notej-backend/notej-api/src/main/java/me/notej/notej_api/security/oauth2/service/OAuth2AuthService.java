package me.notej.notej_api.security.oauth2.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.notej.notej_api.security.oauth2.dto.OAuth2LoginResponse;
import me.notej.notej_api.security.oauth2.user.OAuth2Provider;
import me.notej.notej_api.security.oauth2.user.OAuth2UserUnlinkManager;
import me.notej.notej_api.core.domain.Member;
import me.notej.notej_api.security.oauth2.domain.OAuth2UserConnection;
import me.notej.notej_api.security.common.Role;
import me.notej.notej_api.security.jwt.TokenProvider;
import me.notej.notej_api.security.refresh.domain.RefreshToken;
import me.notej.notej_api.core.repository.MemberRepository;
import me.notej.notej_api.security.oauth2.repository.OAuth2UserConnectionRepository;
import me.notej.notej_api.security.refresh.repository.RefreshTokenRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OAuth2AuthService {
    private final MemberRepository memberRepository;
    private final OAuth2UserConnectionRepository connRepo;
    private final RefreshTokenRepository rtRepo;
    private final TokenProvider tokenProvider;
    private final OAuth2UserUnlinkManager unlinkManager;

    /**
     * 리소스 서버로부터 모든 정보를 받아 최종적으로 성공된 인증 객체인 Authentication를 받아 서버 어플리케이션 상에서 로그인 처리를 마무리하기 위한 로직을 담습니다.
     * 어플리케이션용 회원, 리프레시 토큰 엔티티를 생성 및 DB에 저장하며 어플리케이션에서 생성된 액세스, 리프레시 토큰을 넣은 OAuth2LoginResponse를 반환합니다.
     * 반환된 OAuth2LoginResponse는 최초 요청 클라이언트(로그인 사용자 브라우저)로 넘어가게 될 것입니다.
     * @param principal
     * @return
     */

    public OAuth2LoginResponse login(OAuth2UserPrincipal principal) {
        var info = principal.getUserInfo();
        var provider = info.getProvider();
        var providerId = info.getId();

        // 1) OAuth2UserConnection 기반으로 회원 조회
        Member member = connRepo.findByProviderAndProviderId(provider, providerId)
                .map(OAuth2UserConnection::getMember)
                .map(m -> m.updateName(info.getName(), info.getNickname()))
                .orElseGet(() -> {
                    // 새 Member 생성
                    Member newMember = memberRepository.save(
                            Member.builder()
                                    .email(info.getEmail())
                                    .name(info.getName())
                                    .nickname(info.getNickname())
                                    .memberUuid(UUID.randomUUID().toString())
                                    .roles(Set.of(Role.ROLE_USER))
                                    .build()
                    );

                    // 새 OAuth2UserConnection 생성
                    connRepo.save(
                            OAuth2UserConnection.builder()
                                    .member(newMember)
                                    .provider(provider)
                                    .providerId(providerId)
                                    .linkedAt(Instant.now())
                                    .build()
                    );

                    return newMember;
                });

        // 2) 토큰 발급
        String accessToken = tokenProvider.createToken(member.getMemberUuid());
        String refreshToken = tokenProvider.createRefreshToken(member.getMemberUuid());

        // 3) RefreshToken 저장 or 갱신
        Instant expiry = Instant.now().plusMillis(tokenProvider.getRefreshTokenExpiryMs());
        rtRepo.findTopByMemberIdAndRevokedFalseOrderByExpiryDateDesc(member.getId())
                .ifPresentOrElse(
                        rt -> rt.updateToken(refreshToken, expiry),
                        () -> rtRepo.save(
                                RefreshToken.builder()
                                        .member(member)
                                        .token(refreshToken)
                                        .expiryDate(expiry)
                                        .revoked(false)
                                        .build()
                        )
                );

        return new OAuth2LoginResponse(accessToken, refreshToken);
    }


    @Transactional
    public void unlink(OAuth2UserPrincipal principal) {
        // 1) Member 조회
        String email = principal.getUserInfo().getEmail();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(email + " is not found"));

        // 2) 소셜 공급자 측 API로 연결 해제
        OAuth2Provider provider    = principal.getUserInfo().getProvider();
        String         accessToken = principal.getUserInfo().getAccessToken();
        unlinkManager.unlink(provider, accessToken);

        // 3) DB에 저장된 연결 정보 삭제
        connRepo.deleteByMemberAndProvider(member, provider);

        // 4) 리프레시 토큰 삭제
        rtRepo.deleteByMember(member);
    }
}

