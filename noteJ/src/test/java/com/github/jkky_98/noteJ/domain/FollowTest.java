package com.github.jkky_98.noteJ.domain;

import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.domain.user.UserDesc;
import com.github.jkky_98.noteJ.domain.user.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FollowTest {

    @Test
    @DisplayName("[Follow] 빌더를 통한 객체 생성 테스트")
    void followBuilderTest() {
        // given: 팔로워와 팔로잉 User 생성
        User follower = createTestUser("followerUser", "follower@example.com");
        User following = createTestUser("followingUser", "following@example.com");

        // when: 빌더를 통한 Follow 객체 생성
        Follow follow = Follow.builder()
                .follower(follower)
                .following(following)
                .build();

        // then: 각 필드가 올바르게 설정되었는지 검증
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

    @Test
    @DisplayName("[Follow] updateFollowing 메서드 테스트")
    void updateFollowingTest() {
        // given
        User follower = createTestUser("followerUser", "follower@example.com");
        User following = createTestUser("followingUser", "following@example.com");

        Follow follow = Follow.builder().build();

        // when: updateFollowing 메서드 호출
        follow.updateFollowing(following);

        // then: Follow의 following이 업데이트되고, following의 followerList에 추가되었는지 확인
        assertThat(follow.getFollowing()).isEqualTo(following);
        assertThat(following.getFollowerList()).contains(follow);
    }

    @Test
    @DisplayName("[Follow] matchFollowingByUsername 메서드 테스트")
    void matchFollowingByUsernameTest() {
        // given
        User follower = createTestUser("followerUser", "follower@example.com");
        User following = createTestUser("followingUser", "following@example.com");

        Follow follow = Follow.of(follower, following);

        // when & then: 올바른 username이면 true, 다르면 false
        assertThat(follow.matchFollowingByUsername("followingUser")).isTrue();
        assertThat(follow.matchFollowingByUsername("otherUser")).isFalse();
    }

    @Test
    @DisplayName("[Follow] matchFollowing 메서드 테스트")
    void matchFollowingTest() {
        // given
        User follower = createTestUser("followerUser", "follower@example.com");
        User following = createTestUser("followingUser", "following@example.com");

        Follow follow = Follow.of(follower, following);

        // when & then: 동일한 User 객체일 경우 true, 그렇지 않으면 false
        assertThat(follow.matchFollowing(following)).isTrue();

        User anotherUser = createTestUser("anotherUser", "another@example.com");
        assertThat(follow.matchFollowing(anotherUser)).isFalse();
    }

    // 테스트용 User 객체 생성 헬퍼 메서드
    private User createTestUser(String username, String email) {
        UserDesc userDesc = UserDesc.builder()
                .description("Test description")
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
