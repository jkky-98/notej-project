package me.notej.notej_api.core.blogmain.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import me.notej.notej_api.core.blogmain.domain.Blog;
import me.notej.notej_api.core.blogmain.dto.CompleteProfileRequest;
import me.notej.notej_api.core.member.domain.Member;
import me.notej.notej_api.core.blogmain.repository.BlogRepository;
import me.notej.notej_api.core.member.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InitializeBlogService {

    private final MemberRepository memberRepository;
    private final BlogRepository blogRepository;

    @Transactional
    public void initializeBlog(final String memberUuid, final CompleteProfileRequest request) {

        Member member = memberRepository.findByMemberUuid(memberUuid)
                .orElseThrow(() -> new EntityNotFoundException("유저가 없음: " + memberUuid));

        if (member.getBlog() != null) {
            throw new RuntimeException("ALREADY_INITIALIZED_BLOG");
        }

        if (blogRepository.existsByUrl(request.blogUrl())) {
            throw new IllegalArgumentException("DUPLICATE_BLOG_URL");
        }

        if (request.blogUrl() == null || request.blogUrl().isBlank()) {
            throw new IllegalArgumentException("BLOG_URL_MUST_NOT_BE_EMPTY");
        }

        Blog blog = Blog.builder()
                .username(request.nickname())
                .title(request.blogTitle())
                .url(request.blogUrl())
                .build();
        blogRepository.save(blog);
        member.setBlog(blog);
        member.setCompleted(true);
    }
}
