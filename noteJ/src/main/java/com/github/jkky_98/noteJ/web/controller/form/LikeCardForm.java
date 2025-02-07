package com.github.jkky_98.noteJ.web.controller.form;

import com.github.jkky_98.noteJ.domain.Post;
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

    public static LikeCardForm ofFromPost(Post post) {
        return LikeCardForm.builder()
                .postId(post.getId())
                .postTitle(post.getTitle())
                .postDescription(post.getPostSummary())
                .postUrl(
                        generatePostUrl(post)
                )
                .postThumbnailUrl(post.getThumbnail())
                .build();
    }

    private static String generatePostUrl(Post post) {
        String username = post.getUser().getUsername();
        String postUrl = post.getPostUrl();

        return "/@" + username + "/post/" + postUrl;
    }
}
