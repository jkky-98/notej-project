package com.github.jkky_98.noteJ.web.controller.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LikeStatusResponseDto {
    private boolean liked;

    public static LikeStatusResponseDto of(boolean liked) {
        return LikeStatusResponseDto.builder()
                .liked(liked)
                .build();
    }
}
