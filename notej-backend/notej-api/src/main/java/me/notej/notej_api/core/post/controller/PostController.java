package me.notej.notej_api.core.post.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.notej.notej_api.core.post.dto.*;
import me.notej.notej_api.core.post.service.PostService;
import me.notej.notej_api.global.page.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PutMapping("/api/secure/post/{id}")
    public ResponseEntity<?> updatePost(
            @PathVariable Long id,
            @RequestBody PostUpdateRequest request
    ) {
        log.info("[PostController][updatePost] PostController - updatePost() request : {}", request);
        Long postId = postService.updatePost(id, request);
        return ResponseEntity.ok(postId);
    }

    @GetMapping("/api/secure/post/{id}")
    public ResponseEntity<PostWriteResponse> getPostAuthorized(
            @PathVariable Long id
    ) {
        PostWriteResponse response = postService.getPostWritable(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/post/{postId}")
    public ResponseEntity<PostResponse> getPost(
            @PathVariable Long postId
    ) {
        PostResponse response = postService.getPost(postId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/api/secure/post/{id}")
    public ResponseEntity<?> deletePost(
            @PathVariable Long id
    ) {
        postService.deletePost(id);
        return ResponseEntity.ok("POST_DELETE_SUCCESS");
    }

    @GetMapping("/api/posts/related")
    public ResponseEntity<List<PostViewRelatedPostResponse>> getRelatedPosts(
            @RequestParam Long categoryId,
            @RequestParam Long currentPostId
    ) {
        List<PostViewRelatedPostResponse> relatedPosts = postService.getRelatedPosts(categoryId, currentPostId);
        log.info("[PostController][getRelatedPosts] PostController - getRelatedPosts() relatedPosts : {}", relatedPosts);
        return ResponseEntity.ok(relatedPosts);
    }

    @GetMapping("/api/{blogUrl}/posts")
    public ResponseEntity<PageResponse<PostFilteredResponse>> getPostsFiltered(
            @PathVariable String blogUrl,
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String tagName,
            @RequestParam(defaultValue = "latest") String sortBy,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit
    ) {
        // 서비스 계층 호출 (page는 0부터 시작하므로 1 빼줌)
        Page<PostFilteredResponse> filteredPosts = postService.getFilteredPosts(
                blogUrl, categoryName, search, tagName, sortBy, page - 1, limit);

        // PageResponse 객체로 변환하여 반환
        return ResponseEntity.ok(PageResponse.from(filteredPosts));
    }
}
