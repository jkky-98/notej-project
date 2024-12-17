package com.github.jkky_98.noteJ.web.controller;

import com.github.jkky_98.noteJ.service.PostService;
import com.github.jkky_98.noteJ.web.controller.dto.PostViewDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

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
}
