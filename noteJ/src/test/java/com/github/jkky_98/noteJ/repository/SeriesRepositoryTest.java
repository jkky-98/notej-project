package com.github.jkky_98.noteJ.repository;

import com.github.jkky_98.noteJ.domain.Series;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.domain.user.UserRole;
import com.github.jkky_98.noteJ.web.config.CacheConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)  // 기본 H2 사용 막기
@ActiveProfiles("test")
@Import({CacheConfig.class})
@DisplayName("[SeriesRepository] Integration Tests")
class SeriesRepositoryTest {

    @Autowired
    private SeriesRepository seriesRepository;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("findBySeriesNameAndUser() - 일치하는 시리즈 조회")
    void testFindBySeriesNameAndUser_found() {
        // given: 사용자와 시리즈 생성 및 persist
        User user = User.builder()
                .username("testUser")
                .email("testUser@test.com")
                .password("123456")
                .userRole(UserRole.USER)
                .build();
        em.persist(user);

        Series series = Series.builder()
                .seriesName("Test Series")
                .user(user)
                .build();
        em.persist(series);

        em.flush();
        em.clear();

        // when: 동일한 사용자와 시리즈명을 이용하여 조회
        Optional<Series> found = seriesRepository.findBySeriesNameAndUser("Test Series", user);

        // then: 조회 결과가 존재해야 함
        assertThat(found).isPresent();
        assertThat(found.get().getSeriesName()).isEqualTo("Test Series");
        assertThat(found.get().getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("findBySeriesNameAndUser() - 일치하는 시리즈가 없을 경우")
    void testFindBySeriesNameAndUser_notFound() {
        // given: 사용자 생성
        User user = User.builder()
                .username("testUser")
                .email("testUser@test.com")
                .password("123456")
                .userRole(UserRole.USER)
                .build();
        em.persist(user);
        em.flush();
        em.clear();

        // when: 존재하지 않는 시리즈명을 이용하여 조회
        Optional<Series> found = seriesRepository.findBySeriesNameAndUser("NonExistentSeries", user);

        // then: 결과가 Optional.empty()여야 함
        assertThat(found).isNotPresent();
    }
}