package me.notej.notej_api.core.post.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PostCreateRequest(
        @NotNull
        String title,
        @NotNull
        String content,
        String bio,
        List<String> tags,
        Long categoryId,
        Boolean active,
        String thumbnailUrl
) {
}
