package com.github.jkky_98.noteJ.web.controller.dto;

import com.github.jkky_98.noteJ.domain.Post;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PostNotOpenDto {

    private String title;
    private String postUrl;
    private LocalDateTime createDt;
    private String postSummary;

    public static PostNotOpenDto of(Post post) {
        return PostNotOpenDto.builder()
                .title(post.getTitle())
                .postUrl(post.getPostUrl())
                .createDt(post.getCreateDt())
                .postSummary(post.getPostSummary())
                .build();
    }
}
