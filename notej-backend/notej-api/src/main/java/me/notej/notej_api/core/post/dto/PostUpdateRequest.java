package me.notej.notej_api.core.post.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PostUpdateRequest(
        @NotNull
        String title,
        @NotNull
        String content,
        String shortDescription,
        List<String> tags,
        Long categoryId,
        Boolean isPublic,
        String thumbnailUrl
) {
}
