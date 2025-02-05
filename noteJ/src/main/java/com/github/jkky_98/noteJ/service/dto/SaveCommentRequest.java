package com.github.jkky_98.noteJ.service.dto;

import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.web.controller.CommentController;
import com.github.jkky_98.noteJ.web.controller.form.CommentForm;
import lombok.*;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SaveCommentRequest {
    private CommentForm commentForm;
    private User sessionUser;
    private String postUrl;
    private String username;

    public static SaveCommentRequest of(CommentForm commentForm, User sessionUser, String postUrl, String username) {
        return SaveCommentRequest.builder()
                .commentForm(commentForm)
                .sessionUser(sessionUser)
                .postUrl(postUrl)
                .username(username)
                .build();
    }
}


