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

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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

        String targetUrl = determineTargetUrl(request, response, authentication);

        if (response.isCommitted()) {
            log.debug("[OAuth2AuthenticationSuccessHandler] Response already committed. Can't redirect to {}", targetUrl);
            return;
        }

        log.info("[OAuth2AuthenticationSuccessHandler] Redirecting to {}", targetUrl);
        clearAuthenticationAttributes(request, response);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    protected String determineTargetUrl(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) {

        Optional<String> redirectUri = CookieUtils.getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME)
                .map(Cookie::getValue);

        String targetUrl = redirectUri
                .map(encoded -> URLDecoder.decode(encoded, StandardCharsets.UTF_8))
                .orElse(getDefaultTargetUrl());

        String mode = CookieUtils.getCookie(request, MODE_PARAM_COOKIE_NAME)
                .map(Cookie::getValue)
                .orElse("");

        OAuth2UserPrincipal principal = getOAuth2UserPrincipal(authentication);

        if (principal == null) {
            log.error("[OAuth2AuthenticationSuccessHandler] No principal for authentication {}", authentication.getName());
            return targetUrl + "?error=Login failed";
        }

        if ("login".equalsIgnoreCase(mode)) {
            OAuth2LoginResponse resp = authService.login(principal);

            CookieUtils.addCookie(response, "access_token", resp.accessToken(), 1800); // 30분
            CookieUtils.addCookie(response, "refresh_token", resp.refreshToken(), 604800); // 7일

            log.info("[OAuth2AuthenticationSuccessHandler] Successfully logged in. Redirecting to {}", targetUrl);
            return targetUrl;
        }

        if ("unlink".equalsIgnoreCase(mode)) {
            authService.unlink(principal);
            log.info("[OAuth2AuthenticationSuccessHandler] Successfully unlinked. Redirecting to {}", targetUrl);
            return targetUrl;
        }

        return targetUrl + "?error=Login failed";
    }

    private OAuth2UserPrincipal getOAuth2UserPrincipal(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        return (principal instanceof OAuth2UserPrincipal) ? (OAuth2UserPrincipal) principal : null;
    }

    protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }
}
