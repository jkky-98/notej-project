package me.notej.notej_api.core.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.notej.notej_api.core.service.InitializeBlogService;
import me.notej.notej_api.core.dto.CompleteProfileRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/secure/users")
public class InitializeBlogController {

    private final InitializeBlogService initializeBlogService;

    @PostMapping("/complete-profile")
    public ResponseEntity<?> saveInitialBlog(
            Authentication authentication,
            @RequestBody CompleteProfileRequest request
    ) {

        User user = (User) authentication.getPrincipal();
        String memberUuid = user.getUsername();

        initializeBlogService.initializeBlog(memberUuid, request);

        return ResponseEntity.ok("프로필 설정이 완료되었습니다.");
    }
}
