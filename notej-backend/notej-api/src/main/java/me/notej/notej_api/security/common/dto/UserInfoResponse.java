package me.notej.notej_api.security.common.dto;

public record UserInfoResponse(
        Long id,
        String username,
        String email,
        boolean completed
) {}
