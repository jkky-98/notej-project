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

    private final PostHitsRepository postHitsRepository;

    private final UserRepository userRepository;

    @Transactional
    public PostViewDto getPost(String username, String postUrl, HttpServletRequest request) {

        // 포스트 조회수 증가 로직
        savePostHits(username, postUrl, request);

        User user = userRepository.findByUsername(username).orElseThrow(() -> new EntityNotFoundException("User not found"));

        List<Post> posts = user.getPosts();

        for (Post post : posts) {
            if (post.getPostUrl().equals(postUrl)) {
                PostViewDto postViewDto = new PostViewDto();
                postViewDto.setTitle(post.getTitle());
                postViewDto.setUsername(username);
                postViewDto.setContent(post.getContent());
                postViewDto.setCreateByDt(post.getCreateDt());
                postViewDto.setLikeCount(post.getLikes().size());

                List<String> tags = getTags(post);
                postViewDto.setTags(tags);

                List<CommentsDto> comments = getComments(post);
                postViewDto.setComments(comments);
                return postViewDto;
            }
        }

        throw new EntityNotFoundException("Post not Found");
    }

    private void savePostHits(String username, String postUrl, HttpServletRequest request) {
        Optional<User> sessionUser = SessionUtils.getSessionUser(request);
        sessionUser.ifPresentOrElse(
                user -> {
                    // sessionUser.getUsername()과 입력된 username 비교
                    if (user.getUsername().equals(username)) {
                        return; // 값이 같으면 아무 작업도 하지 않음
                    }

                    // 값이 있을 경우: sessionUser 기반으로 PostHits 생성
                    User userFind = userRepository.findById(user.getId()).orElseThrow(() -> new EntityNotFoundException("User not Found"));
                    Post postFind = postRepository.findByUserUsernameAndPostUrl(username, postUrl).orElseThrow(() -> new EntityNotFoundException("Post not Found"));

                    PostHits postHits = PostHits.builder()
                            .ipAddress(ClientUtils.getRemoteIP(request))
                            .viewedAt(LocalDateTime.now())
                            .user(userFind)
                            .post(postFind)
                            .build();

                    postHitsRepository.save(postHits);
                },
                () -> {
                    // 값이 없을 경우: 기본 PostHits 생성
                    Post postFind = postRepository.findByUserUsernameAndPostUrl(username, postUrl).orElseThrow(() -> new EntityNotFoundException("Post not Found"));

                    PostHits postHits = PostHits.builder()
                            .ipAddress(ClientUtils.getRemoteIP(request))
                            .viewedAt(LocalDateTime.now())
                            .post(postFind)
                            .build();

                    postHitsRepository.save(postHits);
                }
        );
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
