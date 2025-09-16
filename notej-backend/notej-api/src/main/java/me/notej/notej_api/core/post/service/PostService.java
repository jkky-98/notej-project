package me.notej.notej_api.core.post.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.notej.notej_api.core.blogmain.domain.Blog;
import me.notej.notej_api.core.category.domain.Category;
import me.notej.notej_api.core.category.repository.CategoryRepository;
import me.notej.notej_api.core.member.domain.Member;
import me.notej.notej_api.core.member.repository.MemberRepository;
import me.notej.notej_api.core.post.domain.Post;
import me.notej.notej_api.core.post.dto.PostCreateRequest;
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

        log.info("[PostService][savePost] PostService - savePost() newPost : {}", newPost);
        return postSaved.getId();
    }

    @Transactional
    public Long updatePost(Long postId, PostUpdateRequest request) {
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

        // 더티-체킹 업데이트
        post.setTitle(title);
        post.setContent(content);
        post.setBio(shortDescription);
        post.setCategory(category);
        post.setActive(isPublic);
        post.setThumbnail(thumbnailUrl);

        // 태그 업데이트
        // ToDo : 태그 처리 어떻게 할 건지 고민 해보자, Post에 엮인 태그 모두 삭제후 다시 태그 인서트 하는 방식이 가장 쉬운데, 더 생각해보셈
        // ToDo : 프론트엔드 함수 정리좀 하자 너무 방대해짐 분리하고 pinia에서 중앙 관리하던 지금 존나 복잡함
        return null;
    }
}
