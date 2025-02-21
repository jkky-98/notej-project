package com.github.jkky_98.noteJ.repository;

import com.github.jkky_98.noteJ.domain.Post;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.web.config.CacheConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import({CacheConfig.class})
@DisplayName("[PostRepository] Integration Tests")
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private EntityManager em;

    private User user;
    private Post post1; // writable false
    private Post post2; // writable true
    private Post post3; // writable false

    @BeforeEach
    void setUp() {
        // 사용자 생성 및 persist
        user = User.builder()
                .username("testUser")
                .email("testUser@test.com")
                .password("123456")
                .build();
        em.persist(user);

        // 게시글 생성
        post1 = Post.builder()
                .postUrl("url1")
                .title("Post 1")
                .writable(false)
                .user(user)
                .build();
        post2 = Post.builder()
                .postUrl("url2")
                .title("Post 2")
                .writable(true)
                .user(user)
                .build();
        post3 = Post.builder()
                .postUrl("url3")
                .title("Post 3")
                .writable(false)
                .user(user)
                .build();

        em.persist(post1);
        em.persist(post2);
        em.persist(post3);

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("findByPostUrl() - 지정한 postUrl로 Post 조회")
    void testFindByPostUrl() {
        // when
        Optional<Post> found = postRepository.findByPostUrl("url1");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Post 1");
    }

    @Test
    @DisplayName("findAllByUserIdAndWritableFalse() - userId에 해당하며 writable이 false인 Post 조회")
    void testFindAllByUserIdAndWritableFalse() {
        // when
        List<Post> result = postRepository.findAllByUserIdAndWritableFalse(user.getId());

        // then: post1과 post3만 writable이 false이므로 2건이어야 함
        assertThat(result).hasSize(2)
                .extracting("postUrl")
                .containsExactlyInAnyOrder("url1", "url3");
    }
}
