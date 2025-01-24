package com.github.jkky_98.noteJ.web.controller;

import com.github.jkky_98.noteJ.service.PostsService;
import com.github.jkky_98.noteJ.web.controller.dto.PostDto;
import com.github.jkky_98.noteJ.web.controller.form.PostsConditionForm;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PostsApiController {

    private final PostsService postsService;

    @GetMapping("/@{username}/posts")
    public ResponseEntity<List<PostDto>> posts(
                        @PathVariable("username") String usernamePost,
                        @RequestParam("tagName") String tagName,
                        @RequestParam("search") String search,
                        @RequestParam("seriesName") String seriesName,
                        Pageable pageable
    ) {
        PostsConditionForm form = new PostsConditionForm();
        form.setTagName(tagName);
        form.setSearch(search);
        form.setSeriesName(seriesName);

        List<PostDto> postsWithPageableDto = postsService.getPostsWithPageable(usernamePost, form, pageable);

        return ResponseEntity.ok(postsWithPageableDto);
    }
}
