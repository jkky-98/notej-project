package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.Comment;
import com.github.jkky_98.noteJ.domain.Post;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.repository.CommentRepository;
import com.github.jkky_98.noteJ.service.dto.SaveCommentToServiceDto;
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
    public void saveComment(SaveCommentToServiceDto saveCommentToServiceDto) {
        Comment comment = createCommentEntity(saveCommentToServiceDto);
        commentRepository.save(comment);

        // 대댓글의 부모 댓글 유저에게 대댓글 달렸다고 알림
        if (validReplyComment(saveCommentToServiceDto)) {

            Comment commentParent = commentRepository.findById(
                    saveCommentToServiceDto.getCommentForm().getParentsId()).orElseThrow(() -> new EntityNotFoundException("comment not found")
            );

            alarmToParentCommentUser(
                    saveCommentToServiceDto, commentParent
            );

            if (isEqualsParentCommentUserisPostUser(saveCommentToServiceDto, commentParent)) {
                return;
            }
        }

        // 게시글 작성자에게 댓글 알림
        if (validSameUserComment(saveCommentToServiceDto)) {
            alarmToPostUser(saveCommentToServiceDto);
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

    private void alarmToPostUser(SaveCommentToServiceDto saveCommentToServiceDto) {
        User userGetNotification = userService.findUserByUsername(saveCommentToServiceDto.getUsername());
        User userSendNotification = userService.findUserById(saveCommentToServiceDto.getSessionUser().getId());
        Post post = postService.findByPostUrl(saveCommentToServiceDto.getPostUrl());
        notificationService.sendCommentPostNotification(userGetNotification, userSendNotification, post.getTitle());
    }

    private void alarmToParentCommentUser(SaveCommentToServiceDto saveCommentToServiceDto, Comment commentParent) {
        User userSendNotification = userService.findUserById(saveCommentToServiceDto.getSessionUser().getId());
        Post post = postService.findByPostUrl(saveCommentToServiceDto.getPostUrl());
        notificationService.sendCommentParentsNotification(commentParent.getUser(), userSendNotification, post.getTitle());
    }

    private static boolean isEqualsParentCommentUserisPostUser(SaveCommentToServiceDto saveCommentToServiceDto, Comment commentParent) {
        return commentParent.getUser().getUsername().equals(saveCommentToServiceDto.getUsername());
    }

    private boolean validReplyComment(SaveCommentToServiceDto saveCommentToServiceDto) {
        if (saveCommentToServiceDto.getCommentForm().getParentsId() == null) {
            return false;
        }

        Long parentsCommentId = saveCommentToServiceDto.getCommentForm().getParentsId();
        Comment commentParents = commentRepository.findById(parentsCommentId).orElseThrow(() -> new EntityNotFoundException("commentNotFound"));
        Long parentsCommentUserId = commentParents.getUser().getId();
        Long sessionUserId = saveCommentToServiceDto.getSessionUser().getId();

        return saveCommentToServiceDto.getCommentForm().getParentsId() > 0 &&
                !parentsCommentUserId.equals(sessionUserId);
    }

    private static boolean validSameUserComment(SaveCommentToServiceDto saveCommentToServiceDto) {
        return !saveCommentToServiceDto.getUsername().equals(saveCommentToServiceDto.getSessionUser().getUsername());
    }

    private Comment createCommentEntity(SaveCommentToServiceDto saveCommentToServiceDto) {
        // Post와 User 조회
        Post postFind = postService.findByPostUrl(
                saveCommentToServiceDto.getPostUrl()
        );

        User userFindSession = userService.findUserById(saveCommentToServiceDto.getSessionUser().getId());

        // 부모 댓글 조회
        Optional<Comment> parentComment = Optional.ofNullable(saveCommentToServiceDto.getCommentForm().getParentsId())
                .flatMap(commentRepository::findById);

        // Comment 엔티티 생성
        return Comment.of(
                postFind,
                userFindSession,
                saveCommentToServiceDto.getCommentForm().getContent(),
                parentComment
        );
    }
}
