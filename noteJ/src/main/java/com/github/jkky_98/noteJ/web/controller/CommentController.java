package com.github.jkky_98.noteJ.web.controller;

import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.service.CommentService;
import com.github.jkky_98.noteJ.service.PostService;
import com.github.jkky_98.noteJ.web.controller.dto.PostViewDto;
import com.github.jkky_98.noteJ.web.controller.form.CommentForm;
import com.github.jkky_98.noteJ.web.session.SessionConst;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class CommentController {

    private final PostService postService;
    private final CommentService commentService;

    @PostMapping("/@{username}/post/{postUrl}")
    public String writeComment(
            @PathVariable("username") String username, // 포스트 주인의 username
            @PathVariable("postUrl") String postUrl, // 포스트의 url
            @SessionAttribute(SessionConst.LOGIN_USER) User sessionUser, // 코멘트 입력자
            @Validated @ModelAttribute("commentForm") CommentForm commentForm, // 코멘트 폼 데이터
            BindingResult bindingResult,
            @RequestHeader("Referer") String referer,
            Model model
    ) {

        if (bindingResult.hasErrors()) {
            PostViewDto postViewDto = postService.getPost(username, postUrl);

            model.addAttribute("postViewDto", postViewDto);
            return "postView";
        }

        // 쿼리 파라미터 제거한 referer
        referer = removeQueryStringInReferer(referer);

        commentService.saveComment(SaveCommentRequest.of(commentForm, sessionUser, postUrl, username));
        return "redirect:" + (referer != null ? referer : "/");
    }

    private static String removeQueryStringInReferer(String referer) {
        if (referer != null) {
            int queryIndex = referer.indexOf("?");
            if (queryIndex != -1) {
                referer = referer.substring(0, queryIndex); // 쿼리 파라미터 제거
            }
        }
        return referer;
    }

    @Getter
    @AllArgsConstructor
    public static class SaveCommentRequest {
        private final CommentForm commentForm;
        private final User sessionUser;
        private final String postUrl;
        private final String username;

        private static SaveCommentRequest of(CommentForm commentForm, User sessionUser, String postUrl, String username) {
            return new SaveCommentRequest(commentForm, sessionUser, postUrl, username);
        }
    }
}
