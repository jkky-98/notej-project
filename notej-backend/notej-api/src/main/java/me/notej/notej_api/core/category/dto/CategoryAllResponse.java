package me.notej.notej_api.core.category.dto;

import java.util.List;

public record CategoryAllResponse(
        List<CategoryResponse> categoryAll
) {
}
