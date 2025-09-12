package me.notej.notej_api.security.common.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.notej.notej_api.core.domain.Member;
import me.notej.notej_api.core.repository.MemberRepository;
import me.notej.notej_api.security.oauth2.util.CookieUtils;
import me.notej.notej_api.security.refresh.repository.RefreshTokenRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/secure/users")
public class LogoutController {

    private final RefreshTokenRepository refreshTokenRepository;
    private final MemberRepository memberRepository;

    @PostMapping("/logout")
    @Transactional
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();

                log.info("authentication class: {}", principal.getClass());

                String memberUuid = extractMemberUuid(principal);
                Member member = memberRepository.findByMemberUuid(memberUuid).orElseThrow(() -> new EntityNotFoundException("Member not found"));
                log.info("로그아웃 요청: member={}", member.getEmail());

                refreshTokenRepository.findTopByMemberIdAndRevokedFalseOrderByExpiryDateDesc(member.getId())
                        .ifPresent(rt -> {
                            rt.setRevoked(true);
                            refreshTokenRepository.save(rt);
                        });

                // 쿠키 삭제를 CookieUtils 메서드로 처리
                CookieUtils.deleteCookie(request, response, "access_token");
                CookieUtils.deleteCookie(request, response, "refresh_token");
            } else {
                log.warn("로그아웃 요청 시 인증 정보 없음");
            }

            SecurityContextHolder.clearContext();
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("로그아웃 처리 중 예외 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private String extractMemberUuid(Object principal) {
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername(); // 일반 로그인 UUID
        } else if (principal instanceof OAuth2User oAuth2User) {
            return oAuth2User.getName(); // OAuth2UserPrincipal 에서 UUID가 name()으로 반환되었는지 확인
        } else {
            throw new IllegalArgumentException("지원되지 않는 인증 객체 타입: " + principal.getClass());
        }
    }

    private void expireCookie(String name, HttpServletResponse response) {
        Cookie cookie = new Cookie(name, null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // 필요에 따라 false로 조정
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
