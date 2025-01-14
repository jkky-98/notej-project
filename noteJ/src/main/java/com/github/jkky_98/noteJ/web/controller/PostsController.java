package com.github.jkky_98.noteJ.web.controller;

import com.github.jkky_98.noteJ.domain.Series;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.service.FollowService;
import com.github.jkky_98.noteJ.service.PostsService;
import com.github.jkky_98.noteJ.service.ProfileService;
import com.github.jkky_98.noteJ.service.SeriesService;
import com.github.jkky_98.noteJ.web.controller.form.PostsConditionForm;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
@Slf4j
public class PostsController {

    private final PostsService postsService;
    private final SeriesService seriesService;

    @GetMapping("/@{username}/posts")
    public String posts(@PathVariable("username") String usernamePost,
                        @ModelAttribute PostsConditionForm postsConditionForm,
                        @SessionAttribute (name = "loginUser", required = false) User sessionUser,
                        Model model
                        ) {

        model.addAttribute("postsViewDto",
                postsService.getPostsViewDto(
                        usernamePost,
                        postsConditionForm,
                        Optional.ofNullable(sessionUser)
                )
        );

        return "posts";
    }

    @GetMapping("/@{username}/posts/series")
    public String postsSeries(
            @PathVariable("username") String username,
            @SessionAttribute (name = "loginUser", required = false) User sessionUser,
            Model model
            ) {
        model.addAttribute("postsViewDto",
                postsService.getSeriesTabs(
                        username,
                        Optional.ofNullable(sessionUser)
                )
        );
        return "series";
    }

    @PostMapping("/@{username}/posts/series")
    public String postsSeriesSaveSeries(
            @RequestParam String seriesName,
            @SessionAttribute("loginUser") User sessionUser,
            @RequestHeader(value = "Referer", required = false) String referer
            ) {
        seriesService.saveSeries(sessionUser, seriesName);

        if (referer != null && !referer.isEmpty()) {
            return "redirect:" + referer;
        } else {
            return "redirect:/";
        }
    }
}
