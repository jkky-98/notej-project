package com.github.jkky_98.noteJ.domain;

import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.domain.user.UserRole;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LikeTest {

    @Test
    @DisplayName("[Like] 빌더를 통한 객체 생성 테스트")
    void likeBuilderTest() {
        // given
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .userRole(UserRole.USER)
                .build();

        Post post = Post.builder()
                .title("Test Post")
                .content("This is a test post.")
                .build();

        Like like = Like.builder()
                .user(user)
                .post(post)
                .build();

        // then
        assertThat(like).isNotNull();
        assertThat(like.getUser()).isEqualTo(user);
        assertThat(like.getPost()).isEqualTo(post);
    }

    @Test
    @DisplayName("[Like] 기본 상태 테스트")
    void likeDefaultStateTest() {
        // given
        Like like = Like.builder().build();

        // then
        assertThat(like).isNotNull();
        assertThat(like.getUser()).isNull();
        assertThat(like.getPost()).isNull();
    }
}
