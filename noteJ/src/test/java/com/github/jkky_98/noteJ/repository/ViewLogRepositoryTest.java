package com.github.jkky_98.noteJ.repository;

import com.github.jkky_98.noteJ.domain.Post;
import com.github.jkky_98.noteJ.domain.ViewLog;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.web.config.CacheConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({CacheConfig.class})
@DisplayName("[ViewLogRepository] Integration Tests")
class ViewLogRepositoryTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private ViewLogRepository viewLogRepository;

    private Post post;
    private User user;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        // 간단한 User 생성
        user = User.builder()
                .username("testUser")
                .email("testUser@test.com")
                .password("123456")
                .build();
        em.persist(user);

        // Post 생성
        post = Post.builder()
                .title("Test Post")
                .postUrl("unique-post-url")
                .build();
        em.persist(post);

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("existsRecentView: 최근 조회가 존재하면 true 반환")
    void testExistsRecentView_true() {
        // given: timeLimit은 1시간 전
        LocalDateTime timeLimit = now.minusHours(1);
        // 현재 시각의 ViewLog를 생성 (createDt는 자동 생성됨)
        ViewLog viewLog = ViewLog.builder()
                .post(post)
                .identifier("user-123")
                .build();
        em.persist(viewLog);
        em.flush();
        em.clear();

        // when
        boolean exists = viewLogRepository.existsRecentView(post.getId(), "user-123", timeLimit);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsRecentView: 오래된 조회는 false 반환")
    void testExistsRecentView_false() {
        // given: timeLimit은 1시간 전
        LocalDateTime timeLimit = now.minusHours(1);
        // ViewLog를 persist (createDt는 now로 설정됨)
        ViewLog viewLog = ViewLog.builder()
                .post(post)
                .identifier("user-123")
                .build();
        em.persist(viewLog);
        em.flush();

        // createDt를 2시간 전으로 업데이트 (빌더로 설정 불가능하므로 JPQL 업데이트 사용)
        em.createQuery("UPDATE ViewLog v SET v.createDt = :oldTime WHERE v.id = :id")
                .setParameter("oldTime", now.minusHours(2))
                .setParameter("id", viewLog.getId())
                .executeUpdate();
        em.flush();
        em.clear();

        // when
        boolean exists = viewLogRepository.existsRecentView(post.getId(), "user-123", timeLimit);

        // then: 업데이트된 ViewLog는 timeLimit보다 오래되었으므로 false
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("countRecentViews: 지정 identifier의 최근 조회 횟수 반환")
    void testCountRecentViews() {
        // given: timeLimit은 1시간 전
        LocalDateTime timeLimit = now.minusHours(1);

        // user-123에 대한 ViewLog 2건 (현재 시간과 30분 전)
        ViewLog viewLog1 = ViewLog.builder()
                .post(post)
                .identifier("user-123")
                .build();
        ViewLog viewLog2 = ViewLog.builder()
                .post(post)
                .identifier("user-123")
                .build();
        em.persist(viewLog1);
        em.persist(viewLog2);

        // 다른 identifier에 대한 조회 1건 (user-456)
        ViewLog viewLog3 = ViewLog.builder()
                .post(post)
                .identifier("user-456")
                .build();
        em.persist(viewLog3);

        em.flush();
        em.clear();

        // when
        long count = viewLogRepository.countRecentViews("user-123", timeLimit);

        // then: user-123의 경우 최근 조회는 2건
        assertThat(count).isEqualTo(2);
    }
}
