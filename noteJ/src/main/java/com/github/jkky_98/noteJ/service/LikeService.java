package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.Like;
import com.github.jkky_98.noteJ.domain.Post;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.exception.LikeBadRequestClientException;
import com.github.jkky_98.noteJ.exception.LikeSelfSaveClientException;
import com.github.jkky_98.noteJ.repository.LikeRepository;
import com.github.jkky_98.noteJ.service.dto.GetLikeStatusServiceDto;
import com.github.jkky_98.noteJ.web.controller.dto.LikeStatusResponseDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LikeService {

    private final UserService userService;
    private final LikeRepository likeRepository;
    private final PostService postService;
    private final NotificationService notificationService;

    @Transactional
    public LikeStatusResponseDto getLikeStatus(final GetLikeStatusServiceDto dto) {
        User userFind = userService.findUserById(dto.getSessionUserId());
        boolean isLike = likeRepository.existsByUserAndPostPostUrl(userFind, dto.getPostUrl());

        return LikeStatusResponseDto.of(isLike);
    }

    @Transactional
    public void saveLike(String postUrl, boolean liked, Long sessionUserId) {
        if (liked != false) {
            log.error("좋아요 상태 : {}", liked);
            throw new LikeBadRequestClientException("좋아요 상태가 역전되어 있습니다.");
        }

        Post postFind = postService.findByPostUrl(postUrl);
        User userFind = userService.findUserById(sessionUserId);

        if (postFind.getUser().getId() == userFind.getId()) {
            throw new LikeSelfSaveClientException("스스로에게 좋아요는 불가능 합니다.");
        }

        Like like = Like.of(postFind, userFind);
        likeRepository.save(like);

        // 좋아요 알림 처리
        notificationService.sendLikePostNotification(postFind.getUser(), userFind, postFind.getTitle());
    }

    @Transactional
    public void deleteLike(String postUrl, boolean liked, Long sessionUserId) {
        if (liked != true) {
            throw new LikeBadRequestClientException("좋아요 상태가 역전되어 있습니다.");
        }

        Post postFind = postService.findByPostUrl(postUrl);
        User userFind = userService.findUserById(sessionUserId);

        Optional<Like> likeByUserAndPost = likeRepository.findByUserAndPost(userFind, postFind);
        Like like = likeByUserAndPost.orElseThrow(() -> new EntityNotFoundException("like not found"));

        likeRepository.delete(like);

    }
}
