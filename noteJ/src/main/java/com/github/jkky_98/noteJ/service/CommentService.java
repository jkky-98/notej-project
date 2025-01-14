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

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostService postService;
    private final UserService userService;

    @Transactional
    public void saveComment(CommentForm commentForm, User sessionUser, String postUrl, String postUsername) {

        Post postFind = postService.findByUserUsernameAndPostUrl(postUsername, postUrl);
        User userFindSession = userService.findUserById(sessionUser.getId());

        Comment comment = Comment.of(postFind, userFindSession, commentForm.getContent(), commentRepository.findById(commentForm.getParentsId()));

        Comment savedComment = commentRepository.save(comment);
        log.info("[CommentService.saveComment] PostUrl : {}, Comment 등록 id: {}", postUrl, savedComment.getId());
    }
}
