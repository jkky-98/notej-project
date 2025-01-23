package com.github.jkky_98.noteJ.aop;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class RateLimitAspect {

    private final Cache<String, Integer> requestCountsPerIpAddress = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES) // 기본 1분 만료
            .build();

    private final HttpServletRequest request;

    public RateLimitAspect(HttpServletRequest request) {
        this.request = request;
    }

    @Around("@annotation(rateLimit)") // @RateLimit 어노테이션이 있는 메서드에 적용
    public Object rateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        String clientIp = getClientIP();
        int maxRequests = rateLimit.maxRequests();
        int timeWindow = rateLimit.timeWindow();

        // 요청 횟수 확인 및 업데이트
        Integer requests = requestCountsPerIpAddress.getIfPresent(clientIp);
        if (requests == null) {
            requestCountsPerIpAddress.put(clientIp, 1);
        } else if (requests >= maxRequests) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many requests. Try again later.");
        } else {
            requestCountsPerIpAddress.put(clientIp, requests + 1);
        }

        // 요청 허용
        return joinPoint.proceed();
    }

    // 클라이언트 IP 주소 추출
    private String getClientIP() {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
