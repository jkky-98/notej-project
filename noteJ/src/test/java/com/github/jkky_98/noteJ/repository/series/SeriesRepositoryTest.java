package com.github.jkky_98.noteJ.repository.series;

import com.github.jkky_98.noteJ.domain.Series;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.repository.SeriesRepository;
import com.github.jkky_98.noteJ.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class SeriesRepositoryTest {

    @Autowired
    private SeriesRepository seriesRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setup() {
        // 공통적으로 사용할 User 저장
        user = userRepository.save(User.builder()
                .username("Test User")
                .build());
    }

    @Test
    @DisplayName("[SeriesRepository] 시리즈 저장 테스트")
    void saveSeriesTest() {
        // given
        Series series = Series.builder()
                .seriesName("Test Series")
                .user(user)
                .build();

        // when
        Series savedSeries = seriesRepository.save(series);

        // then
        assertThat(savedSeries.getId()).isNotNull();
        assertThat(savedSeries.getSeriesName()).isEqualTo("Test Series");
        assertThat(savedSeries.getUser()).isEqualTo(user);
    }

    @Test
    @DisplayName("[SeriesRepository] 시리즈 조회 테스트")
    void findSeriesByIdTest() {
        // given
        Series series = seriesRepository.save(Series.builder()
                .seriesName("Test Series")
                .user(user)
                .build());

        // when
        Optional<Series> foundSeries = seriesRepository.findById(series.getId());

        // then
        assertThat(foundSeries).isPresent();
        assertThat(foundSeries.get().getSeriesName()).isEqualTo("Test Series");
        assertThat(foundSeries.get().getUser()).isEqualTo(user);
    }

    @Test
    @DisplayName("[SeriesRepository] 시리즈 삭제 테스트")
    void deleteSeriesTest() {
        // given
        Series series = seriesRepository.save(Series.builder()
                .seriesName("Test Series")
                .user(user)
                .build());

        // when
        seriesRepository.delete(series);

        // then
        Optional<Series> deletedSeries = seriesRepository.findById(series.getId());
        assertThat(deletedSeries).isEmpty();
    }

    @Test
    @DisplayName("[SeriesRepository] 시리즈 이름으로 조회 테스트")
    void findSeriesBySeriesNameTest() {
        // given
        Series series = seriesRepository.save(Series.builder()
                .seriesName("Unique Series")
                .user(user)
                .build());

        // when
        Optional<Series> foundSeries = seriesRepository.findBySeriesName("Unique Series");

        // then
        assertThat(foundSeries).isPresent();
        assertThat(foundSeries.get().getSeriesName()).isEqualTo("Unique Series");
        assertThat(foundSeries.get().getUser()).isEqualTo(user);
    }
}
