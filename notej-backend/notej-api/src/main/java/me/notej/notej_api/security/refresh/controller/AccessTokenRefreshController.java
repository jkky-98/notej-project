package me.notej.notej_api.security.refresh.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.notej.notej_api.security.refresh.dto.AccessTokenRefreshResponse;
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

    @PostMapping("/api/oauth2/token")
    public ResponseEntity<?> reSupplyByRefreshToken(
            @RequestParam("grant_type") String grantType,
            @RequestParam("refresh_token") String oldRefresh
    ) {
        AccessTokenRefreshResponse response = accessTokenRefreshService.refresh(grantType, oldRefresh);
        return ResponseEntity.ok(response);
    }
}
