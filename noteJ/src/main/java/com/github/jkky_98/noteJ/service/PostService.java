package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.*;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.repository.*;
import com.github.jkky_98.noteJ.web.controller.dto.PostViewDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final UserService userService;

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

        // 2. Post에 연결된 PostTag의 Tag delete 처리
        List<Tag> tagsRemoved = post.getPostTags().stream()
                .map(
                        postTag -> postTag.getTag()
                ).toList();
        tagRepository.deleteAll(tagsRemoved);

        // 4. Post 삭제
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

    /**
     * Post 적절성 검토 후 postId 뱉음.
     * @param postUrl
     * @param username
     * @param sessionUser
     * @return
     */
    @Transactional
    public Long deleteValidPost(String postUrl, String username, User sessionUser) {
        User userFind = userService.findUserById(sessionUser.getId());
        Post postFind = findByUserUsernameAndPostUrl(username, postUrl);

        // 세션 로그인 유저와 post유저의 유저네임 일치해야 함.
        if (userFind.getId() != postFind.getUser().getId()) {
            throw new IllegalArgumentException("세션유저와 post유저의 유저네임 일치하지 않음");
        }

        return postFind.getId();
    }

    @Transactional
    public void deletePost(String postUrl, String username, User sessionUser) {
        Long postIdDeleted = deleteValidPost(postUrl, username, sessionUser);
        removePost(postIdDeleted);
    }
}
