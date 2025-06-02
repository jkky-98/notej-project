package me.notej.notej_api.security.auth.credentials.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.notej.notej_api.security.auth.credentials.dto.CredentialsLoginRequest;
import me.notej.notej_api.security.auth.credentials.dto.CredentialsLoginResponse;
import me.notej.notej_api.security.auth.credentials.dto.CredentialsSignUpRequest;
import me.notej.notej_api.security.auth.credentials.exception.DuplicateEmailException;
import me.notej.notej_api.security.auth.credentials.exception.SocialLoginExistsException;
import me.notej.notej_api.security.domain.CredentialsUserConnection;
import me.notej.notej_api.security.domain.Member;
import me.notej.notej_api.security.domain.Role;
import me.notej.notej_api.security.jwt.TokenProvider;
import me.notej.notej_api.security.refresh.domain.RefreshToken;
import me.notej.notej_api.security.repository.CredentialsUserConnectionRepository;
import me.notej.notej_api.security.repository.MemberRepository;
import me.notej.notej_api.security.repository.RefreshTokenRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CredentialsService {

    private final AuthenticationManager           authManager;
    private final TokenProvider tokenProvider;
    private final MemberRepository memberRepository;
    private final CredentialsUserConnectionRepository credentialsUserConnectionRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder                 passwordEncoder;

    @Transactional
    public CredentialsLoginResponse login(final CredentialsLoginRequest request) {
        // 1) 자격증명 검증
        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 2) Member 조회
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException(request.email()));

        // 3) 기존 활성(미폐기) 리프레시 토큰 폐기
        refreshTokenRepository.findAllByMemberAndRevokedFalse(member)
                .forEach(token -> {
                    token.revoke();
                    log.debug("Revoking old refresh token: {}", token.getId());
                });

        // 4) 새 JWT 발급
        String accessToken  = tokenProvider.createToken(member.getMemberUuid());
        String refreshToken = tokenProvider.createRefreshToken(member.getMemberUuid());

        // 5) 새 리프레시 토큰 DB 저장
        RefreshToken newToken = RefreshToken.builder()
                .member(member)
                .token(refreshToken)
                .expiryDate(Instant.now().plusMillis(tokenProvider.getRefreshTokenExpiryMs()))
                .revoked(false)
                .build();

        refreshTokenRepository.save(newToken);

        log.info("[CredentialsService][login] Credential Login Success | Member Email : {}", member.getEmail());

        return new CredentialsLoginResponse(accessToken, refreshToken);
    }

    @Transactional
    public void save(final CredentialsSignUpRequest request) {
        // 1) 비밀번호 확인 일치 검사
        validSameCheckPasswordAndConfirmPassword(request);

        // 2) 이메일 중복 검사 // 소셜 로그인 이력 검사
        validEmail(request);

        // 3) 엔티티 생성(& 비밀번호 해싱)
        Member member = Member.builder()
                .email(request.email())
                .name(request.name())
                .memberUuid(UUID.randomUUID().toString())
                .roles(Set.of(Role.ROLE_USER))
                .build();

        CredentialsUserConnection cre = CredentialsUserConnection.builder()
                .member(member)
                .passwordHash(passwordEncoder.encode(request.password()))
                .build();

        memberRepository.save(member);
        credentialsUserConnectionRepository.save(cre);

        log.info("[CredentialsService][save] Credential Save Success | Member Email : {}", member.getEmail());
    }

    private static void validSameCheckPasswordAndConfirmPassword(CredentialsSignUpRequest request) {
        if (!request.password().equals(request.confirmPassword())) {
            throw new IllegalArgumentException("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }
    }

    private void validEmail(CredentialsSignUpRequest request) {
        memberRepository.findByEmail(request.email())
                .ifPresent(member -> {
                    // 소셜 로그인 이력이 있으면
                    member.getConnections().stream().findFirst()
                            .ifPresent(connection -> {
                                throw new SocialLoginExistsException(
                                        String.format("이미 %s 계정으로 가입된 이메일입니다.", connection.getProvider().name())
                                );
                            });

                    // 이외에는 일반 이메일 중복
                    throw new DuplicateEmailException(
                            String.format("이미 사용 중인 이메일입니다: %s", request.email())
                    );
                });
    }
}
