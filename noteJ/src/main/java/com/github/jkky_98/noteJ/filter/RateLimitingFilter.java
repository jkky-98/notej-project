package com.github.jkky_98.noteJ.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Order(1) // 인증 필터보다 먼저 실행
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS = 10; // 분당 최대 요청 수

    private final Cache requestCounts;


    public RateLimitingFilter(CacheManager cacheManager) {
        this.requestCounts = cacheManager.getCache("rateLimitCache");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String key = getRateLimitKey(request);
        Integer count = requestCounts.get(key, Integer.class);

        if (count == null) {
            requestCounts.put(key, 1);
        } else if (count >= MAX_REQUESTS) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Too Many Requests");
            log.warn("Rate limit exceeded for {}", key);
            return;
        } else {
            requestCounts.put(key, count + 1);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * IP 기반 Rate Limit 키 생성
     * - `X-Forwarded-For` 헤더가 존재하면 첫 번째 IP를 사용 (프록시 환경 고려)
     * - 없다면 `request.getRemoteAddr()` 사용
     */
    private String getRateLimitKey(HttpServletRequest request) {
        String ip = getClientIp(request);
        return ip + ":" + request.getRequestURI();
    }

    /**
     * 클라이언트의 실제 IP 주소 반환
     * - `X-Forwarded-For` 헤더가 존재하면 첫 번째 IP 사용 (프록시 환경 고려)
     * - 없다면 `request.getRemoteAddr()` 사용
     */
    private String getClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            return forwardedFor.split(",")[0].trim(); // 여러 개의 IP가 있을 경우 첫 번째 사용
        }
        return request.getRemoteAddr();
    }
}