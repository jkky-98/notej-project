package com.github.jkky_98.noteJ.web.controller;

import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.service.PostsService;
import com.github.jkky_98.noteJ.service.SeriesService;
import com.github.jkky_98.noteJ.web.controller.form.DeleteSeriesRequestForm;
import com.github.jkky_98.noteJ.web.controller.form.SaveSeriesForm;
import com.github.jkky_98.noteJ.web.session.SessionConst;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class SeriesController {

    private final SeriesService seriesService;
    private final PostsService postsService;

    @GetMapping("/@{username}/posts/series")
    public String postsSeries(
            @ModelAttribute("saveSeriesForm") SaveSeriesForm form,
            @PathVariable("username") String username,
            @SessionAttribute(name = SessionConst.LOGIN_USER, required = false) User sessionUser,
            Model model
    ) {
        model.addAttribute("postsForm",
                postsService.getSeriesTabs(
                        username,
                        Optional.ofNullable(sessionUser)
                )
        );
        return "series";
    }

    @PostMapping("/@{username}/posts/series")
    public String postsSeriesSaveSeries(
            @Validated @ModelAttribute("saveSeriesForm") SaveSeriesForm form,
            BindingResult bindingResult,
            @PathVariable("username") String username,
            @SessionAttribute(SessionConst.LOGIN_USER) User sessionUser,
            @RequestHeader(value = "Referer", required = false, defaultValue = "/") String referer,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("postsViewDto",
                    postsService.getSeriesTabs(
                            username,
                            Optional.ofNullable(sessionUser)
                    )
            );
            return "series";
        }

        seriesService.saveSeries(sessionUser, form.getSeriesName());

        return "redirect:" + referer;
    }

    @PostMapping("/@{username}/posts/series/delete")
    public String deleteSeriesWithPosts(
            @PathVariable("username") String username,
            @SessionAttribute(SessionConst.LOGIN_USER) User sessionUser,
            @ModelAttribute DeleteSeriesRequestForm form,
            @RequestHeader(value = "Referer", required = false, defaultValue = "/") String referer
    ) {
        seriesService.deleteSeries(username, sessionUser.getId(), form.getSeriesName());
        return "redirect:" + referer;
    }
}
