package com.github.jkky_98.noteJ.web.controller.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LikeStatusForm {
    private boolean liked;

    public static LikeStatusForm of(boolean liked) {
        return LikeStatusForm.builder()
                .liked(liked)
                .build();
    }
}
