package com.github.jkky_98.noteJ.web.controller;

import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.service.CommentService;
import com.github.jkky_98.noteJ.service.PostService;
import com.github.jkky_98.noteJ.service.dto.SaveCommentToServiceDto;
import com.github.jkky_98.noteJ.web.controller.dto.CommentDeleteForm;
import com.github.jkky_98.noteJ.web.controller.dto.PostViewDto;
import com.github.jkky_98.noteJ.web.controller.form.CommentForm;
import com.github.jkky_98.noteJ.web.session.SessionConst;
import com.github.jkky_98.noteJ.web.util.RefererUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
            PostViewDto postViewDto = postService.getPost(postUrl);

            model.addAttribute("postViewDto", postViewDto);
            return "postView";
        }

        commentService.saveComment(
                SaveCommentToServiceDto.of(commentForm, sessionUser, postUrl, username)
        );

        referer = RefererUtil.removeQueryStringInReferer(referer);

        return "redirect:" + (referer != null ? referer : "/");
    }

    @PostMapping("/comments/delete")
    public String deleteComment(
            @SessionAttribute(SessionConst.LOGIN_USER) User sessionUser,
            CommentDeleteForm commentDeleteRequest,
            @RequestHeader("Referer") String referer
    ) {
        commentService.deleteComment(
                commentDeleteRequest.getCommentId(),
                sessionUser.getId()
        );
        return "redirect:" + referer;
    }

}
