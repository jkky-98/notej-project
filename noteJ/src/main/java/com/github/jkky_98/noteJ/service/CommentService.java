package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.Comment;
import com.github.jkky_98.noteJ.domain.Post;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.repository.CommentRepository;
import com.github.jkky_98.noteJ.web.controller.CommentController;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostService postService;
    private final UserService userService;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public List<Comment> getCommentsByPost(Long postId) {
        List<Comment> rootComments = commentRepository.findByPostIdAndParentIsNull(postId);
        List<Comment> sortedComments = new ArrayList<>();
        for (Comment root : rootComments) {
            dfs(root, sortedComments);
        }
        return sortedComments;
    }

    private void dfs(Comment comment, List<Comment> sortedComments) {
        sortedComments.add(comment);
        for (Comment child : comment.getChildrens()) {
            dfs(child, sortedComments);
        }
    }

    @Transactional
    public void saveComment(CommentController.SaveCommentRequest saveCommentRequest) {
        Comment comment = createCommentEntity(saveCommentRequest);
        Comment savedComment = commentRepository.save(comment);
        log.info("[CommentService.saveComment] PostUrl : {}, Comment 등록 id: {}", saveCommentRequest.getPostUrl(), savedComment.getId());

        // 코멘트 알림 (포스트 작성자에게)
        if (validSameUserComment(saveCommentRequest)) {
            User userGetNotification = userService.findUserByUsername(saveCommentRequest.getUsername());
            User userSendNotification = userService.findUserById(saveCommentRequest.getSessionUser().getId());
            Post post = postService.findByPostUrl(saveCommentRequest.getPostUrl());
            notificationService.sendCommentPostNotification(userGetNotification, userSendNotification, post.getTitle());
        }
        // 대댓글 알림 (대댓글의 부모 댓글을 작성한 유저에게)
        // 조건 : 부모 comment_id 존재, 부모 comment의 user가 sessionUser가 아니어야함.
        if (validReplyComment(saveCommentRequest)) {

            Comment commentParent = commentRepository.findById(saveCommentRequest.getCommentForm().getParentsId()).orElseThrow(() -> new EntityNotFoundException("comment not found"));

            User userSendNotification = userService.findUserById(saveCommentRequest.getSessionUser().getId());
            Post post = postService.findByPostUrl(saveCommentRequest.getPostUrl());
            notificationService.sendCommentParentsNotification(commentParent.getUser(), userSendNotification, post.getTitle());
        }


    }

    private boolean validReplyComment(CommentController.SaveCommentRequest saveCommentRequest) {
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

    private static boolean validSameUserComment(CommentController.SaveCommentRequest saveCommentRequest) {
        return !saveCommentRequest.getUsername().equals(saveCommentRequest.getSessionUser().getUsername());
    }

    private Comment createCommentEntity(CommentController.SaveCommentRequest saveCommentRequest) {
        // Post와 User 조회
        Post postFind = postService.findByUserUsernameAndPostUrl(
                saveCommentRequest.getUsername(),
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

    @Transactional
    public void deleteComment(Long commentId) {
        commentRepository.deleteById(commentId);
    }
}
