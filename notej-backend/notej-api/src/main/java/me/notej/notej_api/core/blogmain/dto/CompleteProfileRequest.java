package me.notej.notej_api.core.blogmain.dto;

public record CompleteProfileRequest(
        String nickname,
        String blogTitle,
        String blogUrl
) {
}
