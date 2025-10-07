package me.notej.notej_api.core.member.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.notej.notej_api.core.blogmain.domain.Blog;
import me.notej.notej_api.core.member.domain.Member;
import me.notej.notej_api.core.member.repository.MemberRepository;
import me.notej.notej_api.security.common.dto.UserInfoResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class UserInfoController {

    private final MemberRepository memberRepository;

    @GetMapping("/api/user")
    public UserInfoResponse getCurrentMember(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("인증되지 않은 사용자");
        }

        //principal은 org.springframework.security.core.userdetails.User
        User user = (User) authentication.getPrincipal();
        String emailOrUUID = user.getUsername(); // token subject로 넣은 memberUUID

        // UUID 또는 email로 조회
        Member member = memberRepository.findByMemberUuid(emailOrUUID)  // 또는 findByEmail()
                .orElseThrow(() -> new UsernameNotFoundException("유저가 없음: " + emailOrUUID));

        Blog blog = member.getBlog();

        return new UserInfoResponse(
                member.getMemberUuid(),
                member.getEmail(),
                member.getNickname(),
                blog != null ? blog.getTitle() : null,
                blog != null ? blog.getUrl() : null,
                blog != null ? blog.getUsername() : null,
                member.isCompleted()
        );
    }


}
