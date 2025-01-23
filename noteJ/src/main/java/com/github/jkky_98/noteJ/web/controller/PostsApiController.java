package com.github.jkky_98.noteJ.web.controller;

import com.github.jkky_98.noteJ.service.PostsService;
import com.github.jkky_98.noteJ.web.controller.dto.PostDto;
import com.github.jkky_98.noteJ.web.controller.form.PostsConditionForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Slf4j
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
        log.info("USERNAME POST: {}", usernamePost);
        log.info("POSTS CONDITION: {}, {}, {}", tagName, search, seriesName );
        PostsConditionForm form = new PostsConditionForm();
        form.setTagName(tagName);
        form.setSearch(search);
        form.setSeriesName(seriesName);

        List<PostDto> postsWithPageableDto = postsService.getPostsWithPageable(usernamePost, form, pageable);

        return ResponseEntity.ok(postsWithPageableDto);
    }
}
