package com.github.jkky_98.noteJ.domain;


import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.domain.user.UserDesc;
import com.github.jkky_98.noteJ.domain.user.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SeriesTest {

    @Test
    @DisplayName("[Series] 빌더 생성 테스트")
    void seriesBuilderCreationTest() {
        // given: 테스트용 User 객체 생성
        User user = createTestUser("testuser", "test@example.com");

        // when: 빌더를 사용해 Series 객체 생성 (user와 seriesName 설정)
        Series series = Series.builder()
                .user(user)
                .seriesName("Test Series")
                .build();

        // then: 각 필드가 올바르게 설정되었는지 검증
        assertThat(series).isNotNull();
        assertThat(series.getUser()).isEqualTo(user);
        assertThat(series.getSeriesName()).isEqualTo("Test Series");
        // @Builder.Default 컬렉션은 null이 아니며 빈 리스트여야 함
        assertThat(series.getPosts()).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("[Series] 기본 상태 테스트")
    void seriesDefaultStateTest() {
        // given: 아무 필드도 설정하지 않고 생성
        Series series = Series.builder().build();

        // then: 기본 상태 값 검증
        // id는 영속화 전이므로 null이어야 함
        assertThat(series.getId()).isNull();
        // user, seriesName은 설정하지 않았으므로 null
        assertThat(series.getUser()).isNull();
        assertThat(series.getSeriesName()).isNull();
        // @Builder.Default 컬렉션은 null이 아니며 빈 리스트여야 함
        assertThat(series.getPosts()).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("[Series] updateUser 메서드 테스트")
    void updateUserTest() {
        // given: 기본 Series 객체 생성 (user는 null 상태)
        Series series = Series.builder().build();
        User newUser = createTestUser("newuser", "newuser@example.com");

        // when: updateUser 메서드를 통해 user 업데이트
        series.updateUser(newUser);

        // then: series의 user 필드가 newUser로 변경되었는지 검증
        assertThat(series.getUser()).isEqualTo(newUser);
    }

    // 헬퍼 메서드: 테스트용 User 객체 생성
    private User createTestUser(String username, String email) {
        UserDesc userDesc = UserDesc.builder()
                .description("Test user description")
                .blogTitle("Test Blog")
                .build();

        return User.builder()
                .username(username)
                .email(email)
                .password("password123")
                .userRole(UserRole.USER)
                .userDesc(userDesc)
                .build();
    }
}