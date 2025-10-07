package me.notej.notej_api.core.blogmain.controller;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.notej.notej_api.core.blogmain.dto.BlogProfileResponse;
import me.notej.notej_api.core.blogmain.dto.BlogProfileUpdateRequest;
import me.notej.notej_api.core.blogmain.dto.SideBarBlogInfoResponse;
import me.notej.notej_api.core.blogmain.service.BlogProfileService;
import me.notej.notej_api.core.blogmain.service.SidebarBlogInfoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
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

    @GetMapping("/api/blog/profile")
    public ResponseEntity<?> getBlogProfileMy(Authentication authentication) {
        BlogProfileResponse blogProfileResponse = blogProfileService.getBlogProfile(authentication);
        return ResponseEntity.ok(blogProfileResponse);
    }

    @PutMapping("/api/blog/profile")
    public ResponseEntity<?> updateBlogProfileMy(Authentication authentication, @RequestBody BlogProfileUpdateRequest request) {
        blogProfileService.updateBlogProfile(authentication, request);
        return ResponseEntity.ok("프로필 설정이 완료되었습니다.");
    }

    @PostMapping("/api/blog/profile-image")
    public ResponseEntity<?> updateProfileImage(
            Authentication authentication, // 너의 UserPrincipal 타입에 맞게
            @RequestPart("profileImage") MultipartFile profileImage) {

        // 1. 유효성 검사 (기본적인 파일 유무)
        if (profileImage.isEmpty()) {
            return ResponseEntity.badRequest().body("이미지 파일이 없습니다.");
        }

        try {
            // 2. 서비스 계층 호출
            String imageUrl = blogProfileService.updateBlogProfileImage(authentication, profileImage);

            // 3. 성공 응답
            return ResponseEntity.ok().body(Map.of("profileImageUrl", imageUrl));

        } catch (IllegalArgumentException e) { // 파일 유효성 검사 실패 등 클라이언트 요청 오류
            log.warn("프로필 이미지 업로드 실패 (클라이언트 오류): {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (EntityNotFoundException e) { // 블로그를 찾을 수 없는 경우
            log.warn("프로필 이미지 업로드 실패 (블로그 없음): {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RuntimeException e) { // S3 업로드 실패 등 서비스 내부 오류
            log.error("프로필 이미지 업로드 중 서버 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("프로필 이미지 업로드 실패: " + e.getMessage());
        }
    }
}
