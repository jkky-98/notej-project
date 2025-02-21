package com.github.jkky_98.noteJ.repository;

import com.github.jkky_98.noteJ.domain.Like;
import com.github.jkky_98.noteJ.domain.Post;
import com.github.jkky_98.noteJ.domain.Series;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.web.config.CacheConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({CacheConfig.class})
@DisplayName("[LikeRepository] Integration Tests")
class LikeRepositoryTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private LikeRepository likeRepository;

    // 테스트용 엔티티
    private User user;           // Post 작성자 (post.user)
    private User userGetLike;    // 좋아요를 누른 사용자 (like.userGetLike)
    private Series series;
    private Post post;
    private Like like;

    @BeforeEach
    void setUp() {
        // 사용자 생성
        user = User.builder()
                .username("testUser")
                .email("testUser@gmail.com")
                .password("123456")
                .build();
        userGetLike = User.builder()
                .username("testUserGetLike")
                .email("testUserGetLike@gmail.com")
                .password("123456")
                .build();
        em.persist(user);
        em.persist(userGetLike);

        // 시리즈 생성
        series = Series.builder()
                .seriesName("Test Series")
                .build();
        em.persist(series);

        // 게시글 생성
        post = Post.builder()
                .title("Test Post Title")
                .postUrl("unique-post-url")
                .writable(true)
                .user(user)
                .series(series)
                .build();
        em.persist(post);

        // 좋아요 생성: like.userGetLike가 좋아요 누른 사용자
        like = Like.builder()
                .post(post)
                .user(user)             // 작성자
                .userGetLike(userGetLike) // 좋아요를 누른 사용자
                .build();
        em.persist(like);

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("existsByUserAndPostPostUrl: 존재 여부 확인 - true")
    void testExistsByUserAndPostPostUrl_true() {
        // given: like 엔티티가 존재하므로, user와 postUrl에 대해 true 반환
        boolean exists = likeRepository.existsByUserAndPostPostUrl(user, "unique-post-url");
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByUserAndPostPostUrl: 존재 여부 확인 - false")
    void testExistsByUserAndPostPostUrl_false() {
        // given: 존재하지 않는 postUrl
        boolean exists = likeRepository.existsByUserAndPostPostUrl(user, "non-existent-url");
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("existsByUserAndPost: 존재 여부 확인 - true")
    void testExistsByUserAndPost_true() {
        // given: like 엔티티는 user와 post로 생성되었으므로 true 반환
        boolean exists = likeRepository.existsByUserAndPost(user, post);
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByUserAndPost: 존재 여부 확인 - false")
    void testExistsByUserAndPost_false() {
        // when: 다른 게시글 생성 후 존재 여부 확인
        Post anotherPost = Post.builder()
                .title("Another Post")
                .postUrl("another-url")
                .writable(true)
                .user(user)
                .series(series)
                .build();
        em.persist(anotherPost);
        em.flush();
        em.clear();

        boolean exists = likeRepository.existsByUserAndPost(user, anotherPost);
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("findByUserAndPost: 좋아요 조회 - 존재하는 경우")
    void testFindByUserAndPost_found() {
        Optional<Like> result = likeRepository.findByUserAndPost(user, post);
        assertThat(result).isPresent();
        assertThat(result.get().getPost().getId()).isEqualTo(post.getId());
    }

    @Test
    @DisplayName("findByUserAndPost: 좋아요 조회 - 존재하지 않는 경우")
    void testFindByUserAndPost_notFound() {
        // when: 다른 게시글 생성
        Post anotherPost = Post.builder()
                .title("Another Post")
                .postUrl("another-url")
                .writable(true)
                .user(user)
                .series(series)
                .build();
        em.persist(anotherPost);
        em.flush();
        em.clear();

        Optional<Like> result = likeRepository.findByUserAndPost(user, anotherPost);
        assertThat(result).isNotPresent();
    }

    @Test
    @DisplayName("countLikesByUserId: 좋아요 총 개수 조회")
    void testCountLikesByUserId() {
        // Query에서 l.userGetLike.id = :userId
        long count = likeRepository.countLikesByUserId(userGetLike.getId());
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("countLikesByUserIdAndSeriesName: 시리즈별 좋아요 개수 조회")
    void testCountLikesByUserIdAndSeriesName() {
        // Query에서 l.userGetLike.id = :userId and l.post.series.seriesName = :seriesName
        long count = likeRepository.countLikesByUserIdAndSeriesName(userGetLike.getId(), "Test Series");
        assertThat(count).isEqualTo(1);
    }
}
