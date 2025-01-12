package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.Comment;
import com.github.jkky_98.noteJ.domain.Post;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.repository.CommentRepository;
import com.github.jkky_98.noteJ.repository.PostRepository;
import com.github.jkky_98.noteJ.repository.UserRepository;
import com.github.jkky_98.noteJ.web.controller.form.CommentForm;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public void saveComment(CommentForm commentForm, User sessionUser, String postUrl, String postUsername) {

        Post findPost = postRepository.findPostByUsernameAndPostUrl(postUsername, postUrl);
        User findSessionUser = userRepository.findById(sessionUser.getId()).orElseThrow(() -> new EntityNotFoundException("SessionUser not Found"));

        // parents 존재시
        Comment parentComment = null;
        if (commentForm.getParentsId() != null) {
            parentComment = commentRepository.findById(commentForm.getParentsId()).orElseThrow(() -> new EntityNotFoundException("Parent Comment not Found"));
        }

        Comment comment = Comment.builder()
                .post(findPost)
                .user(findSessionUser)
                .content(commentForm.getContent())
                .parent(parentComment)
                .build();

        Comment savedComment = commentRepository.save(comment);
        log.info("[CommentService.saveComment] PostUrl : {}, Comment 등록 id: {}", postUrl, savedComment.getId());
    }
}
