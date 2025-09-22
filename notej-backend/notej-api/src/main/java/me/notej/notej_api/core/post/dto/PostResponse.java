package me.notej.notej_api.core.post.dto;

import me.notej.notej_api.core.category.dto.CategoryResponse;

import java.util.List;

public record PostResponse(
    Long id,
    String title,
    String content,
    List<String> tags,
    boolean active,
    CategoryResponse category,
    String thumbnailUrl,
    String bio
) {
}
