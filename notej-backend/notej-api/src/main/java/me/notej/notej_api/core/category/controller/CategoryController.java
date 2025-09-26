package me.notej.notej_api.core.category.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.notej.notej_api.core.category.dto.CategoryAllResponse;
import me.notej.notej_api.core.category.dto.CategoryCreateRequest;
import me.notej.notej_api.core.category.dto.CategoryUpdateRequest;
import me.notej.notej_api.core.category.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/api/blog/{blogUrl}/categories")
    public ResponseEntity<?> getCategories(
            @PathVariable String blogUrl
    ) {
        CategoryAllResponse categoryAll = categoryService.getCategories(blogUrl);
        log.info("[CategoryController][getCategories] CategoryController - getCategories() categoryAll : {}", categoryAll);
        return ResponseEntity.ok(categoryAll);
    }

    @GetMapping("/api/secure/categories")
    public ResponseEntity<?> getCategoriesAuthorized(
            Authentication authentication
    ) {
        CategoryAllResponse categoryAll = categoryService.getCategories(authentication);
        log.info("[CategoryController][getCategories] CategoryController - getCategories() categoryAll : {}", categoryAll);
        return ResponseEntity.ok(categoryAll);
    }

    @PostMapping("/api/secure/blog/categories")
    public ResponseEntity<?> createCategory(
            Authentication authentication,
            @RequestBody CategoryCreateRequest request) {
        log.info("[CategoryController][createCategory] CategoryController - createCategory() request : {}", request);
        categoryService.createCategory(authentication, request);

        return ResponseEntity.ok("CATEGORY_CREATE_SUCCESS");
    }

    @PutMapping("/api/secure/blog/categories")
    public ResponseEntity<?> updateCategory(
            @RequestBody List<CategoryUpdateRequest> requests
    ) {
        log.info("[CategoryController][updateCategory] CategoryController - updateCategory() requests : {}", requests);
        categoryService.updateCategories(requests);
        return ResponseEntity.ok("CATEGORY_UPDATE_SUCCESS");
    }
}
