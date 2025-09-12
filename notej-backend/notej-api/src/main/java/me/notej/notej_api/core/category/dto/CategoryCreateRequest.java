package me.notej.notej_api.core.category.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CategoryCreateRequest(
        @NotBlank(message = "카테고리 이름은 필수입니다")
        String name,
        @Min(value = 1, message = "순서는 1 이상이어야 합니다")
        int seq,
        Long parentId
) {
}
