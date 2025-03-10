package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.*;
import com.github.jkky_98.noteJ.domain.mapper.PostMapper;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.repository.*;
import com.github.jkky_98.noteJ.service.dto.DeletePostToServiceDto;
import com.github.jkky_98.noteJ.web.controller.dto.PostViewDto;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final UserService userService;
    private final PostMapper postMapper;

    @Transactional(readOnly = true)
    public Post findByPostUrl(String postUrl) {
        return postRepository.findByPostUrl(postUrl).orElseThrow(() -> new EntityNotFoundException("Post Not Found"));
    }

    @Transactional(readOnly = true)
    public List<Post> findAll() {
        return postRepository.findAll();
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

    @Transactional(readOnly = true)
    public PostViewDto getPost(String postUrl) {

        Post post = postRepository.findByPostUrl(postUrl).orElseThrow(() -> new EntityNotFoundException("Post not found"));

        return postMapper.toPostViewDto(post);
    }

    @CacheEvict(value = "tagCache", key = "#userId")
    public void evictTagCache(Long userId) {
        // 캐시만 삭제하는 목적이므로 내부 로직은 비워둠
    }


    @Transactional
    public void deletePost(DeletePostToServiceDto dto) {
        Long postIdDeleted = validDeletePostAndGetRemovedPostId(
                dto.getPostUrl(), dto.getSessionUserId()
        );

        Post post = findById(postIdDeleted);
        Long userIdPost = post.getUser().getId();

        // Post에 연결된 PostTag의 Tag delete 처리
        List<Tag> tagsRemoved = post.getPostTags()
                .stream()
                .map(
                        PostTag::getTag
                ).toList();



        tagRepository.deleteAll(tagsRemoved);
        postRepository.delete(post);

        // tag 캐시 삭제
        evictTagCache(userIdPost);

    }


    private Long validDeletePostAndGetRemovedPostId(String postUrl, Long sessionUserId) {
        User userFind = userService.findUserById(sessionUserId);
        Post postFind = findByPostUrl(postUrl);

        // 세션 로그인 유저와 post유저의 유저네임 일치해야 함.
        if (userFind.getId() != postFind.getUser().getId()) {
            throw new IllegalArgumentException("세션유저와 post유저의 유저네임 일치하지 않음");
        }

        return postFind.getId();
    }
}
