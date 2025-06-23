package me.notej.notej_api.core.dto;

public record CompleteProfileRequest(
        String nickname,
        String blogTitle,
        String blogUrl
) {
}
