package me.notej.notej_api.security.auth.oauth2.dto;

/**
 * 인증 어플리케이션 서버에서 로그인을 위해 새로 발급한
 * accessToken, refreshToken을 담은 DTO Response 객체입니다.
 */
public record OAuth2LoginResponse(
        String accessToken,
        String refreshToken
) {}
