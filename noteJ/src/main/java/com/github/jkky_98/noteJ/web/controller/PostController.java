package com.github.jkky_98.noteJ.web.controller;

import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.service.PostService;
import com.github.jkky_98.noteJ.service.PostStatsService;
import com.github.jkky_98.noteJ.service.ViewLogService;
import com.github.jkky_98.noteJ.service.dto.DeletePostToServiceDto;
import com.github.jkky_98.noteJ.web.controller.dto.PostStatsForm;
import com.github.jkky_98.noteJ.web.controller.form.CommentForm;
import com.github.jkky_98.noteJ.web.session.SessionConst;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final PostStatsService postStatsService;
    private final ViewLogService viewLogService;

    @GetMapping("/@{username}/post/{postUrl}")
    public String getPost(
            @PathVariable("postUrl") String postUrl,
            @ModelAttribute("commentForm") CommentForm commentForm,
            Model model,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        model.addAttribute("postViewDto", postService.getPost(postUrl));

        // 조회수 증가 로직
        viewLogService.increaseViewCount(
                postService.findByPostUrl(postUrl),
                request,
                response);

        return "postView";
    }


    @GetMapping("/@{username}/post/{postUrl}/stats")
    public String getPostStats(
            @PathVariable("postUrl") String postUrl,
            @SessionAttribute(SessionConst.LOGIN_USER) User sessionUser,
            Model model
    ) {
        PostStatsForm postStatsForm = postStatsService.getPostStats(postUrl, sessionUser);
        model.addAttribute("postStats", postStatsForm);
        return "postStats";
    }

    @PostMapping("/@{username}/post/{postUrl}/delete")
    public String deletePost(
            @PathVariable("postUrl") String postUrl,
            @PathVariable("username") String username,
            @SessionAttribute(SessionConst.LOGIN_USER) User sessionUser
    ) {

        DeletePostToServiceDto dto = new DeletePostToServiceDto(postUrl, sessionUser.getId());
        postService.deletePost(dto);
        return "redirect:/@"+ username + "/posts";
    }
}
