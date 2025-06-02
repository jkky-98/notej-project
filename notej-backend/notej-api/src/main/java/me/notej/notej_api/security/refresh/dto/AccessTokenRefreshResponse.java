package me.notej.notej_api.security.refresh.dto;

public record AccessTokenRefreshResponse(
        String accessToken,
        String refreshToken,
        long    expiresIn   // 초 단위
) { }
