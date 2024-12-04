package com.github.jkky_98.noteJ.domain;

import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.domain.user.UserDesc;
import com.github.jkky_98.noteJ.domain.user.UserRole;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UserTest {

    @Test
    @DisplayName("[User] 생성 방식 테스트 : 빌더 패턴 적용")
    void UserBuilderTest() {
    	// given
        User testUserEntity = User.builder()
                .username("testname")
                .email("test@gmail.com")
                .password("testpassword")
                .userRole(UserRole.USER)
                .build();
    	// when

    	// then
        assertThat(testUserEntity.getUsername()).isEqualTo("testname");
        assertThat(testUserEntity.getEmail()).isEqualTo("test@gmail.com");
        assertThat(testUserEntity.getPassword()).isEqualTo("testpassword");
        assertThat(testUserEntity.getUserRole()).isEqualTo(UserRole.USER);
    }


    @Test
    @DisplayName("[User] @Builder.Default 초기화 테스트")
    void builderDefaultInitializationTest() {
        // given
        User testuser = createTestUser("testuser", "test@example.com");

        // then
        // Posts 초기화 테스트
        assertThat(testuser.getPosts()).isNotNull();
        assertThat(testuser.getPosts().isEmpty()).isTrue();

        // Likes 초기화 테스트
        assertThat(testuser.getLikes()).isNotNull();
        assertThat(testuser.getLikes().isEmpty()).isTrue();

        // FollowerList 초기화 테스트
        assertThat(testuser.getFollowerList()).isNotNull();
        assertThat(testuser.getFollowerList().isEmpty()).isTrue();

        // FollowingList 초기화 테스트
        assertThat(testuser.getFollowingList()).isNotNull();
        assertThat(testuser.getFollowingList().isEmpty()).isTrue();

        // SeriesList 초기화 테스트
        assertThat(testuser.getSeriesList()).isNotNull();
        assertThat(testuser.getSeriesList().isEmpty()).isTrue();

        // Comments 초기화 테스트
        assertThat(testuser.getComments()).isNotNull();
        assertThat(testuser.getComments().isEmpty()).isTrue();

    }

    private User createTestUser(String username, String email) {
        return User.builder()
                .username(username)
                .email(email)
                .password("password123")
                .userRole(UserRole.USER)
                .build();
    }
}
