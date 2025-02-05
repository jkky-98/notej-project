package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.Comment;
import com.github.jkky_98.noteJ.domain.Post;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.repository.CommentRepository;
import com.github.jkky_98.noteJ.service.dto.SaveCommentRequest;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostService postService;
    private final UserService userService;
    private final NotificationService notificationService;

    @Transactional
    public void saveComment(SaveCommentRequest saveCommentRequest) {
        Comment comment = createCommentEntity(saveCommentRequest);
        commentRepository.save(comment);

        // 대댓글의 부모 댓글 유저에게 대댓글 달렸다고 알림
        if (validReplyComment(saveCommentRequest)) {

            Comment commentParent = commentRepository.findById(
                    saveCommentRequest.getCommentForm().getParentsId()).orElseThrow(() -> new EntityNotFoundException("comment not found")
            );

            alarmToParentCommentUser(
                    saveCommentRequest, commentParent
            );

            if (isEqualsParentCommentUserisPostUser(saveCommentRequest, commentParent)) {
                return;
            }
        }

        // 게시글 작성자에게 댓글 알림
        if (validSameUserComment(saveCommentRequest)) {
            alarmToPostUser(saveCommentRequest);
        }

    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        User user = userService.findUserById(userId);
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new EntityNotFoundException("comment not found"));

        if (!comment.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("댓글의 유저가 일치하지 않습니다.");
        }

        commentRepository.deleteById(commentId);
    }

    private void alarmToPostUser(SaveCommentRequest saveCommentRequest) {
        User userGetNotification = userService.findUserByUsername(saveCommentRequest.getUsername());
        User userSendNotification = userService.findUserById(saveCommentRequest.getSessionUser().getId());
        Post post = postService.findByPostUrl(saveCommentRequest.getPostUrl());
        notificationService.sendCommentPostNotification(userGetNotification, userSendNotification, post.getTitle());
    }

    private void alarmToParentCommentUser(SaveCommentRequest saveCommentRequest, Comment commentParent) {
        User userSendNotification = userService.findUserById(saveCommentRequest.getSessionUser().getId());
        Post post = postService.findByPostUrl(saveCommentRequest.getPostUrl());
        notificationService.sendCommentParentsNotification(commentParent.getUser(), userSendNotification, post.getTitle());
    }

    private static boolean isEqualsParentCommentUserisPostUser(SaveCommentRequest saveCommentRequest, Comment commentParent) {
        return commentParent.getUser().getUsername().equals(saveCommentRequest.getUsername());
    }

    private boolean validReplyComment(SaveCommentRequest saveCommentRequest) {
        if (saveCommentRequest.getCommentForm().getParentsId() == null) {
            return false;
        }

        Long parentsCommentId = saveCommentRequest.getCommentForm().getParentsId();
        Comment commentParents = commentRepository.findById(parentsCommentId).orElseThrow(() -> new EntityNotFoundException("commentNotFound"));
        Long parentsCommentUserId = commentParents.getUser().getId();
        Long sessionUserId = saveCommentRequest.getSessionUser().getId();

        return saveCommentRequest.getCommentForm().getParentsId() > 0 &&
                !parentsCommentUserId.equals(sessionUserId);
    }

    private static boolean validSameUserComment(SaveCommentRequest saveCommentRequest) {
        return !saveCommentRequest.getUsername().equals(saveCommentRequest.getSessionUser().getUsername());
    }

    private Comment createCommentEntity(SaveCommentRequest saveCommentRequest) {
        // Post와 User 조회
        Post postFind = postService.findByPostUrl(
                saveCommentRequest.getPostUrl()
        );

        User userFindSession = userService.findUserById(saveCommentRequest.getSessionUser().getId());

        // 부모 댓글 조회
        Optional<Comment> parentComment = Optional.ofNullable(saveCommentRequest.getCommentForm().getParentsId())
                .flatMap(commentRepository::findById);

        // Comment 엔티티 생성
        return Comment.of(
                postFind,
                userFindSession,
                saveCommentRequest.getCommentForm().getContent(),
                parentComment
        );
    }
}
