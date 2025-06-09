package me.notej.notej_api.security.controller;

public record CompleteProfileRequest(
        String nickname,
        String blogTitle,
        String blogUrl
) {
}
