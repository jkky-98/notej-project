package com.github.jkky_98.noteJ.repository.comment;

import com.github.jkky_98.noteJ.domain.Comment;
import com.github.jkky_98.noteJ.domain.Post;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.repository.CommentRepository;
import com.github.jkky_98.noteJ.repository.PostRepository;
import com.github.jkky_98.noteJ.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;


@DataJpaTest
public class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;
    private Post post;

    @BeforeEach
    void setup() {
        // 공통적으로 필요한 User와 Post를 미리 저장
        user = userRepository.save(User.builder().username("Test User").build());
        post = postRepository.save(Post.builder().title("Test Post").content("This is a test post").build());
    }

    @Test
    @DisplayName("[CommentRepository] 댓글 저장 테스트")
    void saveCommentTest() {
        // given
        Comment comment = Comment.builder()
                .content("This is a test comment")
                .post(post)
                .user(user)
                .build();

        // when
        Comment savedComment = commentRepository.save(comment);

        // then
        assertThat(savedComment.getId()).isNotNull();
        assertThat(savedComment.getContent()).isEqualTo("This is a test comment");
        assertThat(savedComment.getPost()).isEqualTo(post);
        assertThat(savedComment.getUser()).isEqualTo(user);
    }

    @Test
    @DisplayName("[CommentRepository] 댓글 조회 테스트")
    void findCommentByIdTest() {
        // given
        Comment comment = commentRepository.save(Comment.builder()
                .content("This is a test comment")
                .post(post)
                .user(user)
                .build());

        // when
        Optional<Comment> foundComment = commentRepository.findById(comment.getId());

        // then
        assertThat(foundComment).isPresent();
        assertThat(foundComment.get().getContent()).isEqualTo("This is a test comment");
        assertThat(foundComment.get().getPost()).isEqualTo(post);
        assertThat(foundComment.get().getUser()).isEqualTo(user);
    }

    @Test
    @DisplayName("[CommentRepository] 댓글 삭제 테스트")
    void deleteCommentTest() {
        // given
        Comment comment = commentRepository.save(Comment.builder()
                .content("This is a test comment")
                .post(post)
                .user(user)
                .build());

        // when
        commentRepository.delete(comment);

        // then
        Optional<Comment> deletedComment = commentRepository.findById(comment.getId());
        assertThat(deletedComment).isEmpty();
    }

    @Test
    @DisplayName("[CommentRepository] 특정 게시물의 모든 댓글 조회 테스트")
    void findAllByPostTest() {
        // given
        Comment comment1 = commentRepository.save(Comment.builder()
                .content("First comment")
                .post(post)
                .user(user)
                .build());

        Comment comment2 = commentRepository.save(Comment.builder()
                .content("Second comment")
                .post(post)
                .user(user)
                .build());

        // when
        List<Comment> comments = commentRepository.findAll();

        // then
        assertThat(comments).hasSize(2);
        assertThat(comments).extracting("content").containsExactlyInAnyOrder("First comment", "Second comment");
    }

    @Test
    @DisplayName("[CommentRepository] 댓글의 부모 댓글 조회 테스트")
    void findParentCommentTest() {
        // given
        Comment parentComment = commentRepository.save(Comment.builder()
                .content("Parent comment")
                .post(post)
                .user(user)
                .build());

        Comment childComment = commentRepository.save(Comment.builder()
                .content("Child comment")
                .post(post)
                .user(user)
                .parent(parentComment)
                .build());

        // when
        Optional<Comment> foundParent = commentRepository.findById(parentComment.getId());
        Optional<Comment> foundChild = commentRepository.findById(childComment.getId());

        // then
        assertThat(foundParent).isPresent();
        assertThat(foundParent.get().getContent()).isEqualTo("Parent comment");
        assertThat(foundChild).isPresent();
        assertThat(foundChild.get().getParent()).isEqualTo(parentComment);
    }
}

