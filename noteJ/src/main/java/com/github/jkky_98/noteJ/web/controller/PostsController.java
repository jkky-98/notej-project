package com.github.jkky_98.noteJ.web.controller;

import com.github.jkky_98.noteJ.service.ProfileService;
import com.github.jkky_98.noteJ.web.controller.form.ProfileForm;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
public class PostsController {

    private final ProfileService profileService;

    @GetMapping("/@{username}/posts")
    public String posts(@PathVariable("username") String username, Model model, HttpSession session) {
        ProfileForm profileForm = profileService.getProfile(session);

        model.addAttribute("profileForm", profileForm);

        return "posts";
    }

}
