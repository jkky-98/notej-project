package com.github.jkky_98.noteJ.web.controller;

import com.github.jkky_98.noteJ.domain.Post;
import com.github.jkky_98.noteJ.domain.Tag;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.service.PostsService;
import com.github.jkky_98.noteJ.service.ProfileService;
import com.github.jkky_98.noteJ.web.controller.dto.PostDto;
import com.github.jkky_98.noteJ.web.controller.dto.TagCountDto;
import com.github.jkky_98.noteJ.web.controller.form.ProfileForm;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class PostsController {

    private final ProfileService profileService;
    private final PostsService postsService;

    @GetMapping("/@{username}/posts")
    public String posts(@PathVariable("username") String username, Model model, HttpSession session) {
        User sessionUser = (User) session.getAttribute("loginUser");

        ProfileForm profileForm = profileService.getProfile(sessionUser);
        model.addAttribute("profileForm", profileForm);

        List<PostDto> postDtos = postsService.getPosts(sessionUser);
        model.addAttribute("posts", postDtos);
        System.out.println("포스트 디티오" + postDtos);

        List<TagCountDto> tagAlls = postsService.getAllTag(sessionUser);
        model.addAttribute("tags", tagAlls);
        System.out.println("모든 태그" + tagAlls);
        // 현재 활성화된 탭
        model.addAttribute("activeTab", "posts");

        // 검색어 초기값 (없을 시 빈 값)
        model.addAttribute("searchQuery", "");

        return "posts";
    }

}
