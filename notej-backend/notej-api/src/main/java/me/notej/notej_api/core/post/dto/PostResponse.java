package me.notej.notej_api.core.post.dto;

import me.notej.notej_api.core.category.dto.CategoryInPostResponse;

import java.util.List;

public record PostResponse(
    String title,
    String content,
    List<String> tags,
    CategoryInPostResponse category,
    String thumbnailUrl,
    String bio,
    String authorUuid
) {
}
