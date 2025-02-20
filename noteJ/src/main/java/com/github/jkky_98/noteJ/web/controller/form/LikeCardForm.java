package com.github.jkky_98.noteJ.web.controller.form;

import lombok.*;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LikeCardForm {
    private Long postId;
    private String postTitle;
    private String postDescription;
    private String postUrl;
    private String postThumbnailUrl;
}
