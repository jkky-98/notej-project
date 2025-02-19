package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.Comment;
import com.github.jkky_98.noteJ.domain.Post;
import com.github.jkky_98.noteJ.domain.mapper.CommentMapper;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.repository.CommentRepository;
import com.github.jkky_98.noteJ.web.controller.form.CommentForm;
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
    private final CommentMapper commentMapper;

    @Transactional
    public void saveComment(CommentForm commentForm, Long sessionUserId, String postUrl, String username) {

        User sessionUser = userService.findUserById(sessionUserId);
        Post post = postService.findByPostUrl(postUrl);

        Optional<Comment> parentComment = Optional.ofNullable(commentForm.getParentsId())
                .flatMap(commentRepository::findById);

        Comment commentForSave = commentMapper.toCommentForSave(post, sessionUser, commentForm.getContent(), parentComment);

        commentRepository.save(commentForSave);

        // 대댓글의 부모 댓글 유저에게 대댓글 달렸다고 알림
        if (validReplyComment(commentForm.getParentsId(), sessionUserId)) {

            Comment commentParent = commentRepository.findById(
                    commentForm.getParentsId()
                    )
                    .orElseThrow(() -> new EntityNotFoundException("comment not found"));

            alarmToParentCommentUser(
                   sessionUserId, postUrl, post.getUser()
            );

            if (isEqualsParentCommentUserisPostUser(post.getUser().getUsername(), commentParent.getUser().getUsername())) {
                return;
            }
        }

        // 게시글 작성자에게 댓글 알림
        if (validSameUserComment(sessionUser.getUsername(), post.getUser().getUsername())) {
            alarmToPostUser(sessionUserId, postUrl);
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

    private void alarmToPostUser(Long sessionUserId, String postUrl) {
        User userSendNotification = userService.findUserById(sessionUserId);
        Post post = postService.findByPostUrl(postUrl);
        User userGetNotification = userService.findUserByUsername(post.getUser().getUsername());
        notificationService.sendCommentPostNotification(userGetNotification, userSendNotification, post.getTitle());
    }

    private void alarmToParentCommentUser(Long sessionUserId, String postUrl, User parentCommentUser) {
        User userSendNotification = userService.findUserById(sessionUserId);
        Post post = postService.findByPostUrl(postUrl);
        notificationService.sendCommentParentsNotification(parentCommentUser, userSendNotification, post.getTitle());
    }

    private static boolean isEqualsParentCommentUserisPostUser(String usernamePost, String usernameParentComment) {
        return usernameParentComment.equals(usernamePost);
    }

    private boolean validReplyComment(Long parentsCommentId, Long sessionUserId) {
        if (parentsCommentId == null) {
            return false;
        }

        Comment commentParents = commentRepository.findById(parentsCommentId).orElseThrow(() -> new EntityNotFoundException("commentNotFound"));
        Long parentsCommentUserId = commentParents.getUser().getId();

        return parentsCommentId > 0 && !parentsCommentUserId.equals(sessionUserId);
    }

    private static boolean validSameUserComment(String sessionUsername, String usernamePost) {
        return !usernamePost.equals(sessionUsername);
    }
}
