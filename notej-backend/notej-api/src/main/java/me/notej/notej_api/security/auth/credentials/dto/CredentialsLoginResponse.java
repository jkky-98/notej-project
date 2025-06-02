package me.notej.notej_api.security.auth.credentials.dto;


public record CredentialsLoginResponse(
        String accessToken,
        String refreshToken
) {
}
