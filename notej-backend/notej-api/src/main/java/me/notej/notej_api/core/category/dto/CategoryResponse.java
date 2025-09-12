package me.notej.notej_api.core.category.dto;

public record CategoryResponse(
        Long categoryId,
        Long parentId,
        String name,
        int seq
) {
}
