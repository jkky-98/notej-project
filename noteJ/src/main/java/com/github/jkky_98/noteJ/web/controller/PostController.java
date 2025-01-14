package com.github.jkky_98.noteJ.web.controller;

import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.service.PostHitsService;
import com.github.jkky_98.noteJ.service.PostService;
import com.github.jkky_98.noteJ.service.PostStatsService;
import com.github.jkky_98.noteJ.web.controller.dto.PostStatsDto;
import com.github.jkky_98.noteJ.web.controller.form.CommentForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@Controller
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final PostStatsService postStatsService;
    private final PostHitsService postHitsService;

    @GetMapping("/@{username}/post/{postUrl}")
    public String getPost(
            @PathVariable("username") String username,
            @PathVariable("postUrl") String postUrl,
            @ModelAttribute("commentForm") CommentForm commentForm,
            Model model,
            @SessionAttribute(value = "loginUser", required = false) User sessionUser,
            @RequestHeader(value = "X-Forwarded-For", required = false) String xForwardedForHeader
    ) {
        postHitsService.increamentPostView(username, postUrl, Optional.ofNullable(sessionUser), getClientIp(xForwardedForHeader));
        model.addAttribute("postViewDto", postService.getPost(username, postUrl));

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

    private String getClientIp(String xForwardedForHeader) {
        if (xForwardedForHeader == null || xForwardedForHeader.isEmpty()) {
            return "noIP"; // IP 헤더가 없으면 처리
        }
        // X-Forwarded-For 헤더는 IP 목록이 있을 수 있으므로, 첫 번째 IP를 가져옴
        String[] ipAddresses = xForwardedForHeader.split(",");
        return ipAddresses[0].trim(); // 첫 번째 IP 반환
    }
}
