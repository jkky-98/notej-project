package me.notej.notej_api.security.credentials.dto;


public record CredentialsLoginResponse(
        String accessToken,
        String refreshToken
) {
}
