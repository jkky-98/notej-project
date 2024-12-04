package com.github.jkky_98.noteJ.domain;

import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.domain.user.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FollowTest {

    @Test
    @DisplayName("[Follow] 빌더를 통한 객체 생성 테스트")
    void followBuilderTest() {
        // given
        User follower = User.builder()
                .username("follower")
                .email("follower@example.com")
                .password("password123")
                .userRole(UserRole.USER)
                .build();

        User following = User.builder()
                .username("following")
                .email("following@example.com")
                .password("password123")
                .userRole(UserRole.USER)
                .build();

        Follow follow = Follow.builder()
                .follower(follower)
                .following(following)
                .build();

        // then
        assertThat(follow).isNotNull();
        assertThat(follow.getFollower()).isEqualTo(follower);
        assertThat(follow.getFollowing()).isEqualTo(following);
    }

    @Test
    @DisplayName("[Follow] 기본 상태 테스트")
    void followDefaultStateTest() {
        // given
        Follow follow = Follow.builder().build();

        // then
        assertThat(follow).isNotNull();
        assertThat(follow.getFollower()).isNull();
        assertThat(follow.getFollowing()).isNull();
    }
}
