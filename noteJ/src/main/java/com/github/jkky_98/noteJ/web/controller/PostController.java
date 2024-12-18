package com.github.jkky_98.noteJ.web.controller;

import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.service.CommentService;
import com.github.jkky_98.noteJ.service.PostService;
import com.github.jkky_98.noteJ.service.PostStatsService;
import com.github.jkky_98.noteJ.web.controller.dto.PostStatsDto;
import com.github.jkky_98.noteJ.web.controller.dto.PostViewDto;
import com.github.jkky_98.noteJ.web.controller.form.CommentForm;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@Controller
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final PostStatsService postStatsService;
    private final CommentService commentService;

    @GetMapping("/@{username}/post/{postUrl}")
    public String getPost(
            @PathVariable("username") String username,
            @PathVariable("postUrl") String postUrl,
            @ModelAttribute("commentForm") CommentForm commentForm,
            Model model,
            HttpServletRequest request
    ) {
        // 에러 메시지를 모델에 추가

        PostViewDto postViewDto = postService.getPost(username, postUrl, request);

        model.addAttribute("postViewDto", postViewDto);
        return "postView";
    }

    @PostMapping("/@{username}/post/{postUrl}")
    public String writeComment(
            @PathVariable("username") String username,
            @PathVariable("postUrl") String postUrl,
            @SessionAttribute("loginUser") User sessionUser,
            @Validated @ModelAttribute("commentForm") CommentForm commentForm,
            BindingResult bindingResult,
            HttpServletRequest request,
            Model model
    ) {

        if (bindingResult.hasErrors()) {
            System.out.println("검증 오류 발생:");
            PostViewDto postViewDto = postService.getPost(username, postUrl, request);

            model.addAttribute("postViewDto", postViewDto);
            return "postView";
        }

        String referer = request.getHeader("Referer");

        // 쿼리 파라미터 제거 로직
        if (referer != null) {
            int queryIndex = referer.indexOf("?");
            if (queryIndex != -1) {
                referer = referer.substring(0, queryIndex); // 쿼리 파라미터 제거
            }
        }

        commentService.saveComment(commentForm, sessionUser, postUrl, username);
        return "redirect:" + (referer != null ? referer : "/");
    }


    @GetMapping("/@{username}/post/{postUrl}/stats")
    public String getPostStats(
            @PathVariable("postUrl") String postUrl,
            @SessionAttribute("loginUser") User sessionUser,
            Model model
    ) {
        PostStatsDto postStatsDto = postStatsService.getPostStats(postUrl, sessionUser);
        model.addAttribute("postStats", postStatsDto);
        return "postStats";
    }
}
