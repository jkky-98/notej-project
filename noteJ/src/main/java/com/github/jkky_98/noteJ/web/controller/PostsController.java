package com.github.jkky_98.noteJ.web.controller;

import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.service.PostsService;
import com.github.jkky_98.noteJ.service.SeriesService;
import com.github.jkky_98.noteJ.web.controller.dto.PostNotOpenDto;
import com.github.jkky_98.noteJ.web.controller.form.PostsConditionForm;
import com.github.jkky_98.noteJ.web.session.SessionConst;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
                        @SessionAttribute (name = SessionConst.LOGIN_USER, required = false) User sessionUser,
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
            @SessionAttribute (name = SessionConst.LOGIN_USER, required = false) User sessionUser,
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
            @SessionAttribute(SessionConst.LOGIN_USER) User sessionUser,
            @RequestHeader(value = "Referer", required = false, defaultValue = "/") String referer
            ) {
        seriesService.saveSeries(sessionUser, seriesName);

        return "redirect:" + referer;
    }

    @GetMapping("/drafts")
    public String getDrafts(
            @SessionAttribute (name = SessionConst.LOGIN_USER, required = false) User sessionUser,
            Model model
            ) {
        List<PostNotOpenDto> postsNotOpen = postsService.getPostsNotOpen(sessionUser);

        model.addAttribute("postsNotOpen", postsNotOpen);

        return "drafts";
    }
}
