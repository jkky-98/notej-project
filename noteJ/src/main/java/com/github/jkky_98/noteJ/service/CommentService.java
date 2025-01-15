package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.Comment;
import com.github.jkky_98.noteJ.domain.Post;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.repository.CommentRepository;
import com.github.jkky_98.noteJ.web.controller.CommentController;
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

    @Transactional
    public void saveComment(CommentController.SaveCommentRequest saveCommentRequest) {
        Comment comment = createCommentEntity(saveCommentRequest);
        Comment savedComment = commentRepository.save(comment);
        log.info("[CommentService.saveComment] PostUrl : {}, Comment 등록 id: {}", saveCommentRequest.getPostUrl(), savedComment.getId());
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


}
