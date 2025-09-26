package me.notej.notej_api.core.blogmain.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import me.notej.notej_api.aws.s3.ProfileImageService;
import me.notej.notej_api.aws.s3.S3Bucket;
import me.notej.notej_api.core.blogmain.domain.Blog;
import me.notej.notej_api.core.blogmain.dto.BlogUserInfo;
import me.notej.notej_api.core.blogmain.dto.SideBarBlogInfoResponse;
import me.notej.notej_api.core.blogmain.repository.BlogRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SidebarBlogInfoService {

    private final BlogRepository blogRepository;
    private final ProfileImageService profileImageService;

    public SideBarBlogInfoResponse getSideBarBlogInfo(String blogUrl) {
        Blog blog = blogRepository.findByUrl(blogUrl).orElseThrow(() -> new EntityNotFoundException("BLOG_NOT_FOUND"));

        String profileImageUrl = profileImageService.getProfileImageUrl(blog.getProfileImage());

        BlogUserInfo blogUserInfo = new BlogUserInfo(
                blog.getTitle() != null ? blog.getTitle() : "notitle",
                blog.getUsername() != null ? blog.getUsername() : "noname",
                blog.getBio() != null ? blog.getBio() : "",
                profileImageUrl != null ? profileImageUrl : null,
                blog.getUrl() != null ? blog.getUrl() : null
        );

        return new SideBarBlogInfoResponse(blogUserInfo);
    }
}
