package me.notej.notej_api.core.post.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.notej.notej_api.aws.s3.S3Bucket;
import me.notej.notej_api.core.blogmain.domain.Blog;
import me.notej.notej_api.core.category.domain.Category;
import me.notej.notej_api.core.category.dto.CategoryResponse;
import me.notej.notej_api.core.category.repository.CategoryRepository;
import me.notej.notej_api.core.member.domain.Member;
import me.notej.notej_api.core.member.repository.MemberRepository;
import me.notej.notej_api.core.post.domain.Post;
import me.notej.notej_api.core.post.dto.PostCreateRequest;
import me.notej.notej_api.core.post.dto.PostResponse;
import me.notej.notej_api.core.post.dto.PostUpdateRequest;
import me.notej.notej_api.core.post.repository.PostRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PostService {

    private final MemberRepository memberRepository;
    private final CategoryRepository categoryRepository;
    private final PostRepository postRepository;
    private final TagService tagService;
    private final S3Bucket s3Bucket;
    private final PostImageService postImageService;

    @Transactional
    public Long savePost(final Authentication authentication,final PostCreateRequest request) {
        User user = (User) authentication.getPrincipal();
        String memberUuid = user.getUsername();

        Member member = memberRepository.findByMemberUuid(memberUuid).orElseThrow(() -> new EntityNotFoundException("USER_NOT_FOUND"));
        Blog blog = member.getBlog();

        String title = request.title();
        String content = request.content();
        String shortDescription = request.shortDescription();
        List<String> tags = request.tags();
        Long categoryId = request.categoryId();
        Boolean isPublic = request.isPublic();
        String thumbnailUrl = request.thumbnailUrl();

        // 카테고리 존재시 엔티티 불러오기
        Category category = null;
        if (categoryId != null) {
            category = categoryRepository.findById(categoryId).orElseThrow(() -> new EntityNotFoundException("CATEGORY_NOT_FOUND"));
        }

        Post newPost = Post.builder()
                .blog(blog)
                .title(title)
                .content(content)
                .bio(shortDescription)
                .category(category)
                .active(isPublic)
                .thumbnail(thumbnailUrl)
                .build();

        Post postSaved = postRepository.save(newPost);

        // 태그 처리
        for (String tag : tags) {
            tagService.saveTag(tag, newPost.getId());
        }
        // 썸네일 처리

        log.info("[PostService][savePost] PostService - savePost() newPost : {}", newPost);
        return postSaved.getId();
    }

    @Transactional
    public Long updatePost(final Long postId, final PostUpdateRequest request) {

        Post post = postRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("POST_NOT_FOUND"));

        String title = request.title();
        String content = request.content();
        String shortDescription = request.shortDescription();
        List<String> tags = request.tags();
        Long categoryId = request.categoryId();
        Boolean isPublic = request.isPublic();
        String thumbnailUrl = request.thumbnailUrl();

        Category category = null;
        if (categoryId != null) {
            category = categoryRepository.findById(request.categoryId()).orElseThrow(() -> new EntityNotFoundException("CATEGORY_NOT_FOUND"));
        }
        // 썸네일 변경 작업
        String thumbnailUrlBefore = post.getThumbnail();
        if (!thumbnailUrlBefore.equals(thumbnailUrl)) {
            // 기존 썸네일 삭제
            postImageService.deleteImage(thumbnailUrlBefore);
            // 새로운 썸네일은 미리 업로드 되어있음.
        }

        // 더티-체킹 업데이트
        post.setTitle(title);
        post.setContent(content);
        post.setBio(shortDescription);
        post.setCategory(category);
        post.setActive(isPublic);
        post.setThumbnail(thumbnailUrl);

        // 태그 업데이트
        tagService.updateTags(tags, postId);
        return post.getId();
    }

    @Transactional(readOnly = true)
    public PostResponse getPost(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("POST_NOT_FOUND"));

        List<String> tags = tagService.getTagsFromPost(post.getId());

        Long categoryId = null;
        if (post.getCategory() != null) {
            categoryId = post.getCategory().getId();
        }


        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                tags,
                post.isActive(),
                categoryId,
                post.getThumbnail(),
                post.getBio()
        );
    }
}
