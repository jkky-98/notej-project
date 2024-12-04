package com.github.jkky_98.noteJ.domain;


import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.domain.user.UserRole;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SeriesTest {

    @Test
    @DisplayName("[Series] 빌더를 통한 객체 생성 테스트")
    void seriesBuilderTest() {
        // given
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .userRole(UserRole.USER)
                .build();

        String seriesName = "Test Series";

        Series series = Series.builder()
                .user(user)
                .seriesName(seriesName)
                .build();

        // then
        assertThat(series).isNotNull();
        assertThat(series.getUser()).isEqualTo(user);
        assertThat(series.getSeriesName()).isEqualTo(seriesName);
    }

    @Test
    @DisplayName("[Series] 기본 상태 테스트")
    void seriesDefaultStateTest() {
        // given
        Series series = Series.builder().build();

        // then
        assertThat(series).isNotNull();
        assertThat(series.getUser()).isNull();
        assertThat(series.getSeriesName()).isNull();
    }
}
