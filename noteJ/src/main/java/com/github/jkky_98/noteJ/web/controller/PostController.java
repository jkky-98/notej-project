package com.github.jkky_98.noteJ.web.controller;

import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.service.PostService;
import com.github.jkky_98.noteJ.service.PostStatsService;
import com.github.jkky_98.noteJ.web.controller.dto.PostStatsDto;
import com.github.jkky_98.noteJ.web.controller.dto.PostViewDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final PostStatsService postStatsService;

    @GetMapping("/@{username}/post/{postUrl}")
    public String getPost(
        @PathVariable("username") String username,
        @PathVariable("postUrl") String postUrl,
        Model model,
        HttpServletRequest request
    ) {
        PostViewDto postViewDto = postService.getPost(username, postUrl, request);

        model.addAttribute("postViewDto", postViewDto);
        return "postView";
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
