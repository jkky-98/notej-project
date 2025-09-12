package me.notej.notej_api.core.blogmain.controller;

import lombok.RequiredArgsConstructor;
import me.notej.notej_api.core.blogmain.dto.BlogProfileResponse;
import me.notej.notej_api.core.blogmain.dto.BlogProfileUpdateRequest;
import me.notej.notej_api.core.blogmain.dto.SideBarBlogInfoResponse;
import me.notej.notej_api.core.blogmain.service.BlogProfileService;
import me.notej.notej_api.core.blogmain.service.SidebarBlogInfoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class BlogMainController {

    private final SidebarBlogInfoService sidebarBlogInfoService;
    private final BlogProfileService blogProfileService;

    @GetMapping("/api/blog/{blogUrl}/info")
    public ResponseEntity<?> getBlogInfo(
            @PathVariable String blogUrl
    ) {
        SideBarBlogInfoResponse sideBarBlogInfoResponse = sidebarBlogInfoService.getSideBarBlogInfo(blogUrl);

        return ResponseEntity.ok(sideBarBlogInfoResponse);
    }

    @GetMapping("/api/secure/blog/my-profile")
    public ResponseEntity<?> getBlogProfileMy(Authentication authentication) {
        BlogProfileResponse blogProfileResponse = blogProfileService.getBlogProfile(authentication);
        return ResponseEntity.ok(blogProfileResponse);
    }

    @PutMapping("/api/secure/blog/my-profile")
    public ResponseEntity<?> updateBlogProfileMy(Authentication authentication, @RequestBody BlogProfileUpdateRequest request) {
        blogProfileService.updateBlogProfile(authentication, request);
        return ResponseEntity.ok("프로필 설정이 완료되었습니다.");
    }
}
