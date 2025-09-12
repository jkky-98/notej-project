package me.notej.notej_api.security.common.dto;

public record UserInfoResponse(
        String memberUuid,
        String email,
        String nickname,
        String blogTitle,
        String blogUrl,
        String blogUsername,
        boolean completed
) {}
