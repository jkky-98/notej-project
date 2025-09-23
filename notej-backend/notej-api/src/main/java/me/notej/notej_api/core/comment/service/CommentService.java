package me.notej.notej_api.core.comment.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import me.notej.notej_api.core.comment.domain.Comment;
import me.notej.notej_api.core.comment.dto.CommentRequest;
import me.notej.notej_api.core.comment.dto.CommentResponse;
import me.notej.notej_api.core.comment.repository.CommentRepository;
import me.notej.notej_api.core.member.domain.Member;
import me.notej.notej_api.core.member.repository.MemberRepository;
import me.notej.notej_api.core.post.domain.Post;
import me.notej.notej_api.core.post.repository.PostRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    public List<CommentResponse> getCommentsByPostId(Long postId) {
        List<Comment> comments = commentRepository.findAllByPostIdOrderByCreatedAtAsc(postId);

        return comments.stream()
                .map(CommentResponse::fromEntity)
                .toList();
    }

    @Transactional
    public CommentResponse createComment(Authentication authentication, CommentRequest request) {

        User user = (User) authentication.getPrincipal();
        String authorUuid = user.getUsername();

        Post post = postRepository.findById(request.postId())
                .orElseThrow(() -> new EntityNotFoundException("POST_NOT_FOUND"));

        Member author = memberRepository.findByMemberUuid(authorUuid)
                .orElseThrow(() -> new EntityNotFoundException("MEMBER_NOT_FOUND"));

        Comment comment;
        if (request.parentId() != null) {
            Comment parent = commentRepository.findById(request.parentId())
                    .orElseThrow(() -> new EntityNotFoundException("COMMENT_NOT_FOUND"));
            comment = Comment.builder()
                    .content(request.content())
                    .post(post)
                    .member(author)
                    .parent(parent)
                    .build();
        } else {
            comment = Comment.builder()
                    .content(request.content())
                    .post(post)
                    .member(author)
                    .build();
        }

        Comment savedComment = commentRepository.save(comment);
        return CommentResponse.fromEntity(savedComment);
    }
}
