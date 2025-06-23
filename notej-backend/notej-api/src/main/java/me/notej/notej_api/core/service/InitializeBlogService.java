package me.notej.notej_api.core.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import me.notej.notej_api.core.dto.CompleteProfileRequest;
import me.notej.notej_api.core.domain.Member;
import me.notej.notej_api.core.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InitializeBlogService {

    private final MemberRepository memberRepository;

    @Transactional
    public void initializeBlog(final String memberUuid, final CompleteProfileRequest request) {

        Member member = memberRepository.findByMemberUuid(memberUuid)
                .orElseThrow(() -> new EntityNotFoundException("유저가 없음: " + memberUuid));

        if (memberRepository.existsByBlogUrl(request.blogUrl())) {
            throw new IllegalArgumentException("DUPLICATE_BLOG_URL");
        }

        member.setBlogUsername(request.nickname());
        member.setBlogTitle(request.blogTitle());
        member.setCompleted(true);
        member.setBlogUrl(request.blogUrl());
    }
}
