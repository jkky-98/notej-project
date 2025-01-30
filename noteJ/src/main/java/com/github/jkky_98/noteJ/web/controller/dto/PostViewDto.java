package com.github.jkky_98.noteJ.web.controller.dto;

import com.github.jkky_98.noteJ.domain.Comment;
import com.github.jkky_98.noteJ.domain.Post;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PostViewDto {

    private String title;
    private String postUrl;
    private String username;
    private LocalDateTime createByDt;
    @Builder.Default
    private List<String> tags = new ArrayList<>();
    private String content;
    private int likeCount;
    @Builder.Default
    private List<CommentsDto> comments = new ArrayList<>();

    public static PostViewDto of(Post post) {
        return PostViewDto.builder()
                .title(post.getTitle())
                .postUrl(post.getPostUrl())
                .username(post.getUser().getUsername())
                .createByDt(post.getCreateDt())
                .content(post.getContent())
                .tags(
                        post.getPostTags().stream()
                                .map(postTag -> postTag.getTag().getName())
                                .toList()
                )
                .comments(
                        post.getComments().stream()
                                .map(CommentsDto::of)
                                .toList()
                )
                .likeCount(post.getLikes().size())
                .build();
    }
}


