package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.Like;
import com.github.jkky_98.noteJ.domain.Post;
import com.github.jkky_98.noteJ.domain.mapper.LikeMapper;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.exception.LikeBadRequestClientException;
import com.github.jkky_98.noteJ.exception.LikeSelfSaveClientException;
import com.github.jkky_98.noteJ.repository.LikeRepository;
import com.github.jkky_98.noteJ.service.dto.DeleteLikeToServiceDto;
import com.github.jkky_98.noteJ.service.dto.SaveLikeToServiceDto;
import com.github.jkky_98.noteJ.web.controller.dto.LikeListByPostDto;
import com.github.jkky_98.noteJ.web.controller.dto.LikeStatusForm;
import com.github.jkky_98.noteJ.web.controller.form.LikeCardForm;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LikeService {

    private final UserService userService;
    private final LikeRepository likeRepository;
    private final PostService postService;
    private final NotificationService notificationService;
    private final LikeMapper likeMapper;

    @Transactional(readOnly = true)
    public LikeStatusForm getLikeStatus(String postUrl, Long sessionUserId) {
        User userFind = userService.findUserById(sessionUserId);
        Post postFind = postService.findByPostUrl(postUrl);
        boolean isLike = likeRepository.existsByUserAndPost(userFind, postFind);

        return LikeStatusForm.of(isLike);
    }

    @Transactional
    public void saveLike(SaveLikeToServiceDto dto) {
        if (dto.isLiked()) {
            log.error("좋아요 상태 : {}", dto.isLiked());
            throw new LikeBadRequestClientException("좋아요 상태가 역전되어 있습니다.");
        }

        Post postFind = postService.findByPostUrl(dto.getPostUrl());
        User userFind = userService.findUserById(dto.getUserId());

        if (isSelfLike(postFind, userFind)) {
            throw new LikeSelfSaveClientException("스스로에게 좋아요는 불가능 합니다.");
        }

        Like like = likeMapper.toLike(userFind, postFind);
        likeRepository.save(like);

        // 좋아요 알림 처리
        notificationService.sendLikePostNotification(
                postFind.getUser(),
                userFind,
                postFind.getTitle()
        );
    }

    @Transactional
    public void deleteLike(DeleteLikeToServiceDto dto) {
        if (!dto.isLiked()) {
            throw new LikeBadRequestClientException("좋아요 상태가 역전되어 있습니다.");
        }

        Post postFind = postService.findByPostUrl(dto.getPostUrl());
        User userFind = userService.findUserById(dto.getUserId());

        Like like = likeRepository.findByUserAndPost(userFind, postFind).orElseThrow(() -> new EntityNotFoundException("like not found"));

        likeRepository.delete(like);

    }

    @Transactional(readOnly = true)
    public List<LikeListByPostDto> getLikeListByPostUrl(String postUrl) {
        Post post = postService.findByPostUrl(postUrl);

        return post.getLikes()
                .stream()
                .map(like -> likeMapper.toLikeListByPostDto(like.getUser()))
                .toList();
    }

    private static boolean isSelfLike(Post postFind, User userFind) {
        return postFind.getUser().getId().equals(userFind.getId());
    }

    @Transactional(readOnly = true)
    public List<LikeCardForm> getLikeCards(Long userId) {
        User user = userService.findUserById(userId);

        return user.getLikes()
                .stream()
                .map(like -> likeMapper.toLikeCardFormByPost(like.getPost()))
                .toList();
    }
}
