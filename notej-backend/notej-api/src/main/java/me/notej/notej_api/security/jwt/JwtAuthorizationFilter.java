package me.notej.notej_api.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Component
@Slf4j
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private final TokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // CORS Preflight OPTIONS 요청은 필터 건너뜀
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        System.out.println("[JwtAuthorizationFilter] 호출됨: " + request.getRequestURI());  // ✅ 콘솔 로그
        log.info("[JwtAuthorizationFilter] 호출됨: {}", request.getRequestURI());

        String token = resolveToken(request);
        log.info("토큰 추출됨: {}", token);

        if (StringUtils.hasText(token) && tokenProvider.validateToken(token)) {
            log.info("토큰 유효 ✅");
            Authentication authentication = tokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("SecurityContextHolder에 인증정보 저장 완료");
        } else {
            log.warn("토큰 유효하지 않음 또는 없음 ❌");
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        log.debug("Request URI: {}", request.getRequestURI());

        String token = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(token) && token.startsWith(BEARER_PREFIX)) {
            log.debug("Token from Authorization header: {}", token);
            return token.substring(BEARER_PREFIX.length());
        }

        if (request.getCookies() != null) {
            for (var cookie : request.getCookies()) {
                log.debug("Cookie detected: {} = {}", cookie.getName(), cookie.getValue());
                if ("access_token".equals(cookie.getName())) {
                    log.debug("Token from cookie: {}", cookie.getValue());
                    return cookie.getValue();
                }
            }
        }

        return null;
    }

    private boolean shouldSkipCookieParsing(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/login/oauth2/") || uri.startsWith("/oauth2/authorization/");
    }
}
