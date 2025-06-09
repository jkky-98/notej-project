package me.notej.notej_api.security.controller;

public record UserInfoResponse(
        Long id,
        String username,
        String email,
        boolean completed
) {}
