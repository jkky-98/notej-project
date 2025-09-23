package me.notej.notej_api.core.post.dto;

import java.util.List;

public record PostWriteResponse(
    Long id,
    String title,
    String content,
    List<String> tags,
    boolean active,
    Long categoryId,
    String thumbnailUrl,
    String bio
) {
}
