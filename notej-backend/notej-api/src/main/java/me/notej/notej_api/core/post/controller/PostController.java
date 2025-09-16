package me.notej.notej_api.core.post.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.notej.notej_api.core.post.dto.PostCreateRequest;
import me.notej.notej_api.core.post.dto.PostUpdateRequest;
import me.notej.notej_api.core.post.service.PostService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
public class PostController {

    private final PostService postService;

    @PostMapping("/api/secure/post")
    public ResponseEntity<?> createPost(
            Authentication authentication,
            @RequestBody PostCreateRequest request
    ) {
        log.info("[PostController][createPost] PostController - createPost() request : {}", request);
        Long postId = postService.savePost(authentication, request);
        return ResponseEntity.ok(postId);
    }

    @PutMapping("/api/secure/post")
    public ResponseEntity<?> updatePost(
            @RequestParam Long postId,
            @RequestBody PostUpdateRequest request
    ) {
        postService.updatePost(postId, request);
    }

}
