package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.*;
import com.github.jkky_98.noteJ.repository.*;
import com.github.jkky_98.noteJ.web.controller.dto.PostViewDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final TagRepository tagRepository;

    @Transactional(readOnly = true)
    public Post findByPostUrl(String postUrl) {
        return postRepository.findByPostUrl(postUrl).orElseThrow(() -> new EntityNotFoundException("Post Not Found"));
    }

    /**
     * postId로 Post 엔티티 가져오기
     * @param postId
     * @return
     */
    @Transactional(readOnly = true)
    public Post findById(Long postId) {
        return postRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("Post not found"));
    }

    /**
     * 게시글 살펴보기 GET 요청시
     * @param usernamePost
     * @param postUrl
     * @return
     */
    @Transactional(readOnly = true)
    public PostViewDto getPost(String usernamePost, String postUrl) {

        Post post = postRepository.findPostByUsernameAndPostUrl(usernamePost, postUrl);

        /**
         * 요구사항
         * PostViewDto
         * Post 정보 가져야 함.
         * Comment 정보 가져야 함.
         * Like 정보 가져야 함.
         */
        return PostViewDto.of(post);
    }

    public Post findByUserUsernameAndPostUrl(String username, String postUrl) {
        return postRepository.findByUserUsernameAndPostUrl(username, postUrl).orElseThrow(() -> new EntityNotFoundException("Post not found"));
    }

    @Transactional
    public void removePost(Long postId) {
        Post post = findById(postId);

        // 2. Post에 연결된 PostTag 처리
        post.getPostTags().forEach(postTag -> {
            // 연관관계 해제
            postTag.getTag().removePostTag(postTag);
            post.removePostTag(postTag);

            // 고아 상태가 된 Tag 삭제
            if (postTag.getTag().getPostTags().isEmpty()) {
                tagRepository.delete(postTag.getTag());
            }
        });

        // 3. Post 삭제
        postRepository.delete(post);
    }

    @Transactional
    public void removeTagInPost(Long postId, Long tagId) {
        Post post = findById(postId);

        post.getPostTags().stream()
                .filter(postTag -> postTag.getTag().getId().equals(tagId))
                .findFirst()
                .ifPresent(postTag -> {
                    // 양방향에서 연관관계 끊기
                    Tag targetTag = postTag.getTag();
                    targetTag.removePostTag(postTag);
                    post.removePostTag(postTag);

                    if (targetTag.getPostTags().isEmpty()) {
                        tagRepository.delete(targetTag);
                    }
                });
    }

}
