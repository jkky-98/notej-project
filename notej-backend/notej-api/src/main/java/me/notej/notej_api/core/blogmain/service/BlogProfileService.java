package me.notej.notej_api.core.blogmain.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.notej.notej_api.aws.s3.ProfileImageService;
import me.notej.notej_api.aws.s3.S3Bucket;
import me.notej.notej_api.core.blogmain.domain.Blog;
import me.notej.notej_api.core.blogmain.dto.BlogProfileResponse;
import me.notej.notej_api.core.blogmain.dto.BlogProfileUpdateRequest;
import me.notej.notej_api.core.member.domain.Member;
import me.notej.notej_api.core.member.repository.MemberRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class BlogProfileService {

    private final MemberRepository memberRepository;
    private final S3Bucket s3Bucket;
    private final ProfileImageService profileImageService;

    @Transactional(readOnly = true)
    public BlogProfileResponse getBlogProfile(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        String memberUuid = user.getUsername();

        Member member = memberRepository.findByMemberUuid(memberUuid).orElseThrow(() -> new EntityNotFoundException("USER_NOT_FOUND"));
        Blog blog = member.getBlog();

        String profileImageUrl = profileImageService.getProfileImageUrl(blog.getProfileImage());

        return new BlogProfileResponse(
                blog.getTitle(),
                blog.getBio(),
                profileImageUrl
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

    @Transactional
    public String updateBlogProfileImage(Authentication authentication, MultipartFile imageFile) {
        User user = (User) authentication.getPrincipal();
        String memberUuid = user.getUsername();

        Member member = memberRepository.findByMemberUuid(memberUuid).orElseThrow(() -> new EntityNotFoundException("USER_NOT_FOUND"));
        Blog blog = member.getBlog();

        // 1. 기존 프로필 이미지가 S3에 업로드 된 것이라면 삭제 먼저
        if (blog.getProfileImage() != null) {
            log.info("[BlogProfileService][updateBlogProfileImage] BlogProfileService - 기존 프로필 이미지 S3 스토리지에서 삭제 : {}", blog.getProfileImage());
        }

        // 2. 새 프로필 이미지 S3에 업로드
        String s3ImageObjectKey = s3Bucket.createImage(imageFile, "profile-images");

        // 3. 저장한 s3 이미지 경로를 DB에 저장
        blog.setProfileImage(s3ImageObjectKey);

        return s3ImageObjectKey;
    }
}
