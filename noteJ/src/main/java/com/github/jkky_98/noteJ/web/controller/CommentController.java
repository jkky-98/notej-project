package com.github.jkky_98.noteJ.web.controller;

import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.service.CommentService;
import com.github.jkky_98.noteJ.service.PostService;
import com.github.jkky_98.noteJ.web.controller.dto.PostViewDto;
import com.github.jkky_98.noteJ.web.controller.form.CommentForm;
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

//    @PostMapping("/@{username}/post/{postUrl}")
//    public String writeComment(
//            @PathVariable("username") String username, // 포스트 주인의 username
//            @PathVariable("postUrl") String postUrl, // 포스트의 url
//            @SessionAttribute("loginUser") User sessionUser, // 코멘트 입력자
//            @Validated @ModelAttribute("commentForm") CommentForm commentForm, // 코멘트 폼 데이터
//            @RequestHeader("Referer") String referer,
//            BindingResult bindingResult,
//            Model model
//    ) {
//
//        if (bindingResult.hasErrors()) {
//            System.out.println("검증 오류 발생:");
//            PostViewDto postViewDto = postService.getPost(username, postUrl);
//
//            model.addAttribute("postViewDto", postViewDto);
//            return "postView";
//        }
//
//        // 쿼리 파라미터 제거 로직
//        if (referer != null) {
//            int queryIndex = referer.indexOf("?");
//            if (queryIndex != -1) {
//                referer = referer.substring(0, queryIndex); // 쿼리 파라미터 제거
//            }
//        }
//
//        commentService.saveComment(commentForm, sessionUser, postUrl, username);
//        return "redirect:" + (referer != null ? referer : "/");
//    }
}
