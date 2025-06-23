package me.notej.notej_api.security.refresh.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.notej.notej_api.security.oauth2.util.CookieUtils;
import me.notej.notej_api.common.exception.ErrorResponse;
import me.notej.notej_api.security.refresh.service.AccessTokenRefreshService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AccessTokenRefreshController {

    private final AccessTokenRefreshService accessTokenRefreshService;

    @PostMapping("/api/refresh")
    public ResponseEntity<?> reSupplyByRefreshToken(
            @RequestParam(value = "grant_type", required = false) String grantType,
            @RequestParam(value = "refresh_token", required = false) String oldRefresh,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        log.info("재발급 로직 시작");
        // 쿠키 기반 fallback
        if (oldRefresh == null) {
            oldRefresh = CookieUtils.getCookie(request, "refresh_token")
                    .map(Cookie::getValue)
                    .orElse(null);
        }

        // 기본값 보정
        if (grantType == null) {
            grantType = "refresh_token";
        }

        // 잘못된 요청 처리
        if (!"refresh_token".equals(grantType) || oldRefresh == null) {
            return ResponseEntity
                    .badRequest()
                    .body(ErrorResponse.of(400, "Invalid grant_type or refresh_token"));
        }

        // 4. 재발급
        var tokenResponse = accessTokenRefreshService.refresh(grantType, oldRefresh);

        // 쿠키로 토큰 다시 설정
        CookieUtils.addCookie(response, "access_token", tokenResponse.accessToken(), 1800);     // 30분
        CookieUtils.addCookie(response, "refresh_token", tokenResponse.refreshToken(), 1296000); // 15일

        return ResponseEntity.ok(tokenResponse);
    }
}
