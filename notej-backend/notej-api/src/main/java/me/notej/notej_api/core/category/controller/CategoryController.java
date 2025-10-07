package me.notej.notej_api.core.category.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.notej.notej_api.core.blogmain.service.BlogService;
import me.notej.notej_api.core.category.dto.CategoryAllResponse;
import me.notej.notej_api.core.category.dto.CategoryCreateRequest;
import me.notej.notej_api.core.category.dto.CategoryUpdateRequest;
import me.notej.notej_api.core.category.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;
    private final BlogService blogService;

    @GetMapping("/api/{blogUrl}/categories")
    public ResponseEntity<?> getCategories(
            @PathVariable String blogUrl,
            Authentication authentication
    ) {
        // blogUrl = "me"일 경우
        String targetBlogUrl = blogUrl;

        if ("me".equalsIgnoreCase(blogUrl)) {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            User user = (User) authentication.getPrincipal();
            String memberUuid = user.getUsername();
            targetBlogUrl = blogService.getBlogUrlByMemberUuid(memberUuid);
        }
        CategoryAllResponse categoryAll = categoryService.getCategories(targetBlogUrl);
        log.info("[CategoryController][getCategories] CategoryController - getCategories() categoryAll : {}", categoryAll);
        return ResponseEntity.ok(categoryAll);
    }

    @PostMapping("/api/{blogUrl}/categories")
    public ResponseEntity<?> createCategory(
            @PathVariable String blogUrl,
            @RequestBody CategoryCreateRequest request) {
        log.info("[CategoryController][createCategory] CategoryController - createCategory() request : {}", request);
        categoryService.createCategory(blogUrl, request);

        return ResponseEntity.ok("CATEGORY_CREATE_SUCCESS");
    }

    @PutMapping("/api/{blogUrl}/categories")
    public ResponseEntity<?> updateCategory(
            @PathVariable String blogUrl,
            @RequestBody List<CategoryUpdateRequest> requests
    ) {
        log.info("[CategoryController][updateCategory] CategoryController - updateCategory() requests : {}", requests);
        categoryService.updateCategories(requests, blogUrl);
        return ResponseEntity.ok("CATEGORY_UPDATE_SUCCESS");
    }
}
