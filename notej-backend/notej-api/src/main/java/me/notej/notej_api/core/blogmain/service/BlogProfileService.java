package me.notej.notej_api.core.blogmain.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.notej.notej_api.core.blogmain.domain.Blog;
import me.notej.notej_api.core.blogmain.dto.BlogProfileResponse;
import me.notej.notej_api.core.blogmain.dto.BlogProfileUpdateRequest;
import me.notej.notej_api.core.blogmain.repository.BlogRepository;
import me.notej.notej_api.core.domain.Member;
import me.notej.notej_api.core.repository.MemberRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BlogProfileService {

    private final BlogRepository blogRepository;
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public BlogProfileResponse getBlogProfile(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        String memberUuid = user.getUsername();

        Member member = memberRepository.findByMemberUuid(memberUuid).orElseThrow(() -> new EntityNotFoundException("USER_NOT_FOUND"));
        Blog blog = member.getBlog();

        return new BlogProfileResponse(
                blog.getTitle(),
                blog.getBio()
        );
    }

    @Transactional
    public void updateBlogProfile(Authentication authentication, BlogProfileUpdateRequest request) {
        User user = (User) authentication.getPrincipal();
        String memberUuid = user.getUsername();

        Member member = memberRepository.findByMemberUuid(memberUuid).orElseThrow(() -> new EntityNotFoundException("USER_NOT_FOUND"));
        Blog blog = member.getBlog();

        blog.setTitle(request.title());
        blog.setBio(request.bio());
        log.info("[BlogProfileService][updateBlogProfile] BlogProfileService - updateBlogProfile() blog : {}", blog);
    }
}
