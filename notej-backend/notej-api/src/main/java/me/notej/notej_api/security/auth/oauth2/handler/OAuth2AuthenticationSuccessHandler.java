package me.notej.notej_api.security.auth.oauth2.handler;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.notej.notej_api.security.HttpCookieOAuth2AuthorizationRequestRepository;
import me.notej.notej_api.security.auth.oauth2.dto.OAuth2LoginResponse;
import me.notej.notej_api.security.auth.oauth2.service.OAuth2AuthService;
import me.notej.notej_api.security.auth.oauth2.service.OAuth2UserPrincipal;
import me.notej.notej_api.security.auth.oauth2.util.CookieUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Optional;

import static me.notej.notej_api.security.HttpCookieOAuth2AuthorizationRequestRepository.MODE_PARAM_COOKIE_NAME;
import static me.notej.notej_api.security.HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME;

@Slf4j
@RequiredArgsConstructor
@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;
    private final OAuth2AuthService authService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        String targetUrl;

        targetUrl = determineTargetUrl(request, response, authentication);

        if (response.isCommitted()) {
            log.debug("[OAuth2AuthenticationSuccessHandler][onAuthenticationSuccess] Response has already been committed. Unable to redirect to {}", targetUrl);
            return;
        }

        clearAuthenticationAttributes(request, response);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) {

        Optional<String> redirectUri = CookieUtils.getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME)
                .map(Cookie::getValue);

        String targetUrl = redirectUri.orElse(getDefaultTargetUrl());

        String mode = CookieUtils.getCookie(request, MODE_PARAM_COOKIE_NAME)
                .map(Cookie::getValue)
                .orElse("");

        OAuth2UserPrincipal principal = getOAuth2UserPrincipal(authentication);

        if (principal == null) {
            log.error("[OAuth2AuthenticationSuccessHandler][onAuthenticationSuccess] No principal found for user {}", authentication.getName());
            return UriComponentsBuilder.fromUriString(targetUrl)
                    .queryParam("error", "Login failed")
                    .build().toUriString();
        }

        if ("login".equalsIgnoreCase(mode)) {
            // ● 로그인 처리: DB 저장/업데이트, 토큰 발급·저장까지
            OAuth2LoginResponse resp = authService.login(principal);
            log.info("[OAuth2AuthenticationSuccessHandler][onAuthenticationSuccess] Successfully logged in to {}", targetUrl);
            return UriComponentsBuilder.fromUriString(targetUrl)
                    .queryParam("access_token",  resp.accessToken())
                    .queryParam("refresh_token", resp.refreshToken())
                    .build().toUriString();

        } else if ("unlink".equalsIgnoreCase(mode)) {
            // ● 연결 해제 처리: DB 삭제, 토큰 삭제
            log.info("[OAuth2AuthenticationSuccessHandler][onAuthenticationSuccess] Successfully unlinked user {}", authentication.getName());
            authService.unlink(principal);

            return UriComponentsBuilder.fromUriString(targetUrl)
                    .build().toUriString();
        }

        return UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("error", "Login failed")
                .build().toUriString();
    }

    private OAuth2UserPrincipal getOAuth2UserPrincipal(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof OAuth2UserPrincipal) {
            return (OAuth2UserPrincipal) principal;
        }
        return null;
    }

    protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }
}
