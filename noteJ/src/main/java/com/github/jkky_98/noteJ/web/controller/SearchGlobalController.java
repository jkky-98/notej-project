package com.github.jkky_98.noteJ.web.controller;

import com.github.jkky_98.noteJ.service.PostsService;
import com.github.jkky_98.noteJ.web.controller.dto.PostDto;
import com.github.jkky_98.noteJ.web.controller.dto.SearchGlobalCondition;
import com.github.jkky_98.noteJ.web.controller.form.PostsConditionForm;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class SearchGlobalController {

    private final PostsService postsService;

    @GetMapping("/search-global")
    public String getSearchGlobal() {
        return "search";
    }

    @GetMapping("/search-global/search")
    public String search(
            @ModelAttribute SearchGlobalCondition cond,
            @PageableDefault Pageable pageable,
            Model model
    ) {
        if (cond.getKeyword() == null || cond.getKeyword().isEmpty()) {
            return "redirect:/search-global";
        }

        List<PostDto> posts = postsService.getPostsGlobalWithPaging(cond, pageable);
        model.addAttribute("posts", posts);

        return "searchGlobalResult";
    }
}
