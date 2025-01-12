package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.*;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.repository.*;
import com.github.jkky_98.noteJ.web.ClientUtils;
import com.github.jkky_98.noteJ.web.controller.dto.CommentsDto;
import com.github.jkky_98.noteJ.web.controller.dto.PostViewDto;
import com.github.jkky_98.noteJ.web.session.SessionUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    private final TagRepository tagRepository;
    /**
     * postId로 Post 엔티티 가져오기
     * @param postId
     * @return
     */
    @Transactional
    public Post findById(Long postId) {
        return postRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("Post not found"));
    }

    /**
     * 게시글 살펴보기 GET 요청시
     * @param usernamePost
     * @param postUrl
     * @return
     */
    @Transactional
    public PostViewDto getPost(String usernamePost, String postUrl) {

        Post post = postRepository.findPostByUsernameAndPostUrl(usernamePost, postUrl);

        /**
         * 요구사항
         * PostViewDto
         * Post 정보 가져야 함.
         * Comment 정보 가져야 함.
         * Like 정보 가져야 함.
         */
        PostViewDto postViewDto = setPostViewDto(usernamePost, post);
        return postViewDto;
    }

    private static PostViewDto setPostViewDto(String usernamePost, Post post) {
        PostViewDto postViewDto = new PostViewDto();
        postViewDto.setId(post.getId());
        postViewDto.setTitle(post.getTitle());
        postViewDto.setUsername(usernamePost);
        postViewDto.setContent(post.getContent());
        postViewDto.setCreateByDt(post.getCreateDt());
        postViewDto.setLikeCount(post.getLikes().size());
        postViewDto.setTags(getTags(post));
        postViewDto.setComments(getComments(post));
        return postViewDto;
    }

    public Post findByUserUsernameAndPostUrl(String username, String postUrl) {
        return postRepository.findByUserUsernameAndPostUrl(username, postUrl).orElseThrow(() -> new EntityNotFoundException("Post not found"));
    }

    private static List<CommentsDto> getComments(Post post) {

        List<CommentsDto> returnCommentsDto = new ArrayList<>();

        List<Comment> comments = post.getComments();
        for (Comment comment : comments) {
            CommentsDto commentsDto = new CommentsDto();
            commentsDto.setCreateBy(comment.getCreateBy());
            commentsDto.setCreateByDt(comment.getCreateDt());
            commentsDto.setContent(comment.getContent());
            commentsDto.setId(comment.getId());
            commentsDto.setParentsId(comment.getParent() != null ? comment.getParent().getId() : null);

            returnCommentsDto.add(commentsDto);
        }

        return returnCommentsDto;
    }

    private static List<String> getTags(Post post) {
        List<String> tags = new ArrayList<>();
        for (PostTag postTag : post.getPostTags()) {
            tags.add(postTag.getTag().getName());
        }
        return tags;
    }

    @Transactional
    public void removePost(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("Post not found"));

        // 2. Post에 연결된 PostTag 처리
        for (PostTag postTag : new ArrayList<>(post.getPostTags())) {
            Tag tag = postTag.getTag();

            // PostTag 제거
            tag.removePostTag(postTag);
            post.removePostTag(postTag);

            // 고아 상태가 된 Tag 삭제
            if (tag.getPostTags().isEmpty()) {
                tagRepository.delete(tag);
            }
        }

        // 3. Post 삭제
        postRepository.delete(post);
    }

    @Transactional
    public void removeTagInPost(Long postId, Long tagId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("Post not found"));

        for (PostTag postTag : new ArrayList<>(post.getPostTags())) {
            if (postTag.getTag().getId().equals(tagId)) {
                // 양방향에서 연관관계 끊기
                Tag targetTag = postTag.getTag();
                targetTag.removePostTag(postTag);
                post.removePostTag(postTag);

                if (targetTag.getPostTags().isEmpty()) {
                    tagRepository.delete(targetTag);
                }
                return;
            }
        }
    }
}
