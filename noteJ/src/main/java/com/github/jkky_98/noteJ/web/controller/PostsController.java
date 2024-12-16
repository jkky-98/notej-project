package com.github.jkky_98.noteJ.web.controller;

import com.github.jkky_98.noteJ.domain.Series;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.service.PostsService;
import com.github.jkky_98.noteJ.service.ProfileService;
import com.github.jkky_98.noteJ.web.controller.dto.PostDto;
import com.github.jkky_98.noteJ.web.controller.dto.TagCountDto;
import com.github.jkky_98.noteJ.web.controller.form.PostsConditionForm;
import com.github.jkky_98.noteJ.web.controller.form.ProfileForm;
import com.github.jkky_98.noteJ.web.controller.form.SeriesViewForm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class PostsController {

    private final ProfileService profileService;
    private final PostsService postsService;

    @GetMapping("/@{username}/posts")
    public String posts(@PathVariable("username") String username,
                        @ModelAttribute PostsConditionForm postsConditionForm,
                        Model model
                        ) {

        ProfileForm profileForm = profileService.getProfile(username);
        model.addAttribute("profileForm", profileForm);

        List<PostDto> posts = postsService.getPosts(username, postsConditionForm);

        model.addAttribute("posts", posts);

        List<TagCountDto> tagAlls = postsService.getAllTag(username);
        model.addAttribute("tags", tagAlls);

        model.addAttribute("username", username);

        // 현재 활성화된 탭
        model.addAttribute("activeTab", "posts");

        return "posts";
    }

    @GetMapping("/@{username}/posts/series")
    public String postsSeries(@PathVariable("username") String username, Model model, HttpServletRequest request) {

        ProfileForm profileForm = profileService.getProfile(username);
        model.addAttribute("profileForm", profileForm);

        model.addAttribute("username", username);

        model.addAttribute("currentUri", request.getRequestURI());

        List<SeriesViewForm> series = postsService.getSeries(username);
        model.addAttribute("seriesList", series);

        // 현재 활성화된 탭
        model.addAttribute("activeTab", "posts");

        // 검색어 초기값 (없을 시 빈 값)
        model.addAttribute("searchQuery", "");

        return "series";
    }

    @PostMapping("/@{username}/posts/series")
    public String postsSeriesAddSeries(@RequestParam String seriesName, HttpSession session) {
        User sessionUser = (User) session.getAttribute("loginUser");

        if (sessionUser == null) {
            return "redirect:/login";
        }

        Series series = postsService.saveSeries(sessionUser, seriesName);
        return "redirect:/";
    }

    @GetMapping("/@{username}/posts/desc")
    public String postsDesc(@PathVariable("username") String username, Model model, HttpServletRequest request) {

        ProfileForm profileForm = profileService.getProfile(username);
        model.addAttribute("profileForm", profileForm);

        model.addAttribute("username", username);

        model.addAttribute("currentUri", request.getRequestURI());

        // 현재 활성화된 탭
        model.addAttribute("activeTab", "posts");

        // 검색어 초기값 (없을 시 빈 값)
        model.addAttribute("searchQuery", "");

        return "postsDesc";
    }
}
