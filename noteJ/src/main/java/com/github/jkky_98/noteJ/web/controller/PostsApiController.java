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
                        @ModelAttribute PostsConditionForm form,
                        Pageable pageable
    ) {


        List<PostDto> postsWithPageableDto = postsService.getPostsWithPageable(usernamePost, form, pageable);

        return ResponseEntity.ok(postsWithPageableDto);
    }
}
