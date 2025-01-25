package com.github.jkky_98.noteJ.web.controller.dto;

import com.github.jkky_98.noteJ.domain.Post;
import com.github.jkky_98.noteJ.domain.user.User;
import lombok.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PostDto {
    private String title;
    private String postSummary;
    private String postUrl;
    private String thumbnail;
    private boolean writable;
    private List<String> tags;
    private int commentCount;
    private int likeCount;
    private LocalDateTime createByDt;
    private String username;

    // 정적 팩토리 메서드
    public static PostDto of(Post post, User user) {
        return PostDto.builder()
                .title(post.getTitle())
                .postSummary(post.getPostSummary())
                .postUrl(post.getPostUrl())
                .thumbnail(post.getThumbnail())
                .writable(post.getWritable())
                .createByDt(post.getCreateDt())
                .username(user.getUsername())
                .tags(
                        post.getPostTags().stream()
                                .map(postTag -> postTag.getTag().getName())
                                .collect(Collectors.toList())
                )
                .commentCount(post.getComments().size())
                .likeCount(post.getLikes().size())
                .build();
    }
}
