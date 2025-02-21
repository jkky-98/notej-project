package com.github.jkky_98.noteJ.repository;

import com.github.jkky_98.noteJ.domain.Comment;
import com.github.jkky_98.noteJ.domain.Post;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.repository.post.PostRepositoryImpl;
import com.github.jkky_98.noteJ.web.config.CacheConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@Import({CacheConfig.class})
@DisplayName("[CommentRepository] Integration Tests")
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("findByPostIdAndParentIsNull() - 부모 댓글만 반환")
    void testFindByPostIdAndParentIsNull() {
        // given
        // 1. 사용자, 게시글, 그리고 댓글들을 생성합니다.
        User user = User.builder()
                .username("testUser")
                .email("testUser@gmail.com")
                .password("123456")
                .build();
        em.persist(user);

        Post post = Post.builder()
                .title("Test Post")
                .postUrl("test-url")
                .user(user)
                .build();
        em.persist(post);

        // 부모가 null인 루트 댓글 2건 생성
        Comment rootComment1 = Comment.builder()
                .content("Root Comment 1")
                .post(post)
                .user(user)
                .build();
        Comment rootComment2 = Comment.builder()
                .content("Root Comment 2")
                .post(post)
                .user(user)
                .build();
        em.persist(rootComment1);
        em.persist(rootComment2);

        // 대댓글(자식 댓글) 생성: 부모로 rootComment1을 설정
        Comment childComment = Comment.builder()
                .content("Child Comment")
                .post(post)
                .user(user)
                .parent(rootComment1)
                .build();
        em.persist(childComment);

        // 영속성 컨텍스트 flush 및 clear (쿼리 실행 보장)
        em.flush();
        em.clear();

        // when
        List<Comment> rootComments = commentRepository.findByPostIdAndParentIsNull(post.getId());

        // then
        // 반환된 댓글은 부모가 null인 루트 댓글 2건이어야 합니다.
        assertThat(rootComments).hasSize(2);
        assertThat(rootComments)
                .extracting("content")
                .containsExactlyInAnyOrder("Root Comment 1", "Root Comment 2");
    }
}
