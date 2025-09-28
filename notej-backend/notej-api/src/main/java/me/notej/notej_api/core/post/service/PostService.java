package me.notej.notej_api.core.post.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.notej.notej_api.aws.s3.S3Bucket;
import me.notej.notej_api.core.blogmain.domain.Blog;
import me.notej.notej_api.core.category.domain.Category;
import me.notej.notej_api.core.category.dto.CategoryInPostResponse;
import me.notej.notej_api.core.category.repository.CategoryRepository;
import me.notej.notej_api.core.member.domain.Member;
import me.notej.notej_api.core.member.repository.MemberRepository;
import me.notej.notej_api.core.post.domain.Post;
import me.notej.notej_api.core.post.domain.PostTag;
import me.notej.notej_api.core.post.dto.*;
import me.notej.notej_api.core.post.repository.PostRepository;
import me.notej.notej_api.core.post.repository.PostRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    private final PostRepositoryCustom PostRepositoryImpl;

    @Transactional
    public Long savePost(final Authentication authentication,final PostCreateRequest request) {
        User user = (User) authentication.getPrincipal();
        String memberUuid = user.getUsername();

        Member member = memberRepository.findByMemberUuid(memberUuid).orElseThrow(() -> new EntityNotFoundException("USER_NOT_FOUND"));
        Blog blog = member.getBlog();

        String title = request.title();
        String content = request.content();
        String bio = request.bio();
        List<String> tags = request.tags();
        Long categoryId = request.categoryId();
        Boolean active = request.active();
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
                .bio(bio)
                .category(category)
                .active(active)
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
        String bio = request.bio();
        List<String> tags = request.tags();
        Long categoryId = request.categoryId();
        Boolean active = request.active();
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
        post.setBio(bio);
        post.setCategory(category);
        post.setActive(active);
        post.setThumbnail(thumbnailUrl);

        // 태그 업데이트
        tagService.updateTags(tags, postId);
        return post.getId();
    }

    @Transactional(readOnly = true)
    public PostWriteResponse getPostWritable(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("POST_NOT_FOUND"));

        List<String> tags = tagService.getTagsFromPost(post.getId());

        Long categoryId = null;
        if (post.getCategory() != null) {
            categoryId = post.getCategory().getId();
        }


        return new PostWriteResponse(
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

    @Transactional(readOnly = true)
    public PostResponse getPost(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("POST_NOT_FOUND"));

        List<String> tags = tagService.getTagsFromPost(post.getId());

        CategoryInPostResponse category = null;
        if (post.getCategory() != null) {
            category = new CategoryInPostResponse(
                    post.getCategory().getId(),
                    post.getCategory().getName()
            );
        }
        String memberUuid = postRepository.findMemberUuidByPostId(post.getId()).orElseThrow(() -> new EntityNotFoundException("MEMBER_NOT_FOUND"));

        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                tags,
                category,
                post.getThumbnail(),
                post.getBio(),
                memberUuid
        );
    }

    public void deletePost(Long id) {
        // post 조회
        Post post = postRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("POST_NOT_FOUND"));
        List<PostTag> postTags = post.getPostTags();

        // 태그 삭제
        postTags.forEach(pt -> tagService.deletePostTagAndTag(pt.getId()));

        // 썸네일 삭제
        postImageService.deleteImage(post.getThumbnail());

        // post 삭제
        postRepository.delete(post);
    }

    @Transactional(readOnly = true)
    public List<PostViewRelatedPostResponse> getRelatedPosts(Long categoryId, Long currentPostId) {
        Post post = postRepository.findById(currentPostId).orElseThrow(() -> new EntityNotFoundException("POST_NOT_FOUND"));
        LocalDateTime updatedAt = post.getUpdated_at();

        List<Post> postsRelated = postRepository.findByCategoryIdAndCreatedAtLessThanEqual(categoryId, updatedAt, PageRequest.of(0, 4));
        return postsRelated.stream()
                .map(PostViewRelatedPostResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<PostFilteredResponse> getFilteredPosts(String blogUrl, String categoryName, String search, String tagName, String sortBy, int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<Post> postPage = PostRepositoryImpl.findFilteredPosts(blogUrl, categoryName, search, tagName, sortBy, pageable);
        return postPage.map(PostFilteredResponse::fromEntity);
    }
}
