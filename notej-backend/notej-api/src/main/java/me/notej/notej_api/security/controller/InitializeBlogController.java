package me.notej.notej_api.security.controller;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.notej.notej_api.security.domain.Member;
import me.notej.notej_api.security.exception.ErrorResponse;
import me.notej.notej_api.security.repository.MemberRepository;
import me.notej.notej_api.security.service.InitializeBlogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class InitializeBlogController {

    private final InitializeBlogService initializeBlogService;

    @PostMapping("/complete-profile")
    public ResponseEntity<?> saveInitialBlog(
            Authentication authentication,
            @RequestBody CompleteProfileRequest request
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.of(HttpStatus.UNAUTHORIZED.value(), "인증되지 않은 사용자입니다"));
        }

        User user = (User) authentication.getPrincipal();
        String memberUuid = user.getUsername();

        initializeBlogService.initializeBlog(memberUuid, request);

        return ResponseEntity.ok("프로필 설정이 완료되었습니다.");
    }
}
