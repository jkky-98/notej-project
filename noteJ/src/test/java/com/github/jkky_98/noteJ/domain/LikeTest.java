package com.github.jkky_98.noteJ.domain;

import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.domain.user.UserRole;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LikeTest {

    private User user;
    private Post oldPost;
    private Post newPost;
    private Like like;

    @BeforeEach
    void setup() {
        // 공통 객체 초기화
        user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .userRole(UserRole.USER)
                .build();

        oldPost = Post.builder()
                .title("Old Post")
                .content("This is the old post content.")
                .build();

        newPost = Post.builder()
                .title("New Post")
                .content("This is the new post content.")
                .build();

        like = Like.builder()
                .user(user)
                .post(oldPost)
                .build();
    }

    @Test
    @DisplayName("[Like] 빌더를 통한 객체 생성 테스트")
    void likeBuilderTest() {
        // when
        Like newLike = Like.builder()
                .user(user)
                .post(oldPost)
                .build();

        // then
        assertThat(newLike).isNotNull();
        assertThat(newLike.getUser()).isEqualTo(user);
        assertThat(newLike.getPost()).isEqualTo(oldPost);
    }

    @Test
    @DisplayName("[Like] 기본 상태 테스트")
    void likeDefaultStateTest() {
        // when
        Like defaultLike = Like.builder().build();

        // then
        assertThat(defaultLike).isNotNull();
        assertThat(defaultLike.getUser()).isNull();
        assertThat(defaultLike.getPost()).isNull();
    }

    @Test
    @DisplayName("[Like] updatePost 메서드 테스트")
    void updatePostTest() {
        // when
        like.updatePost(newPost);

        // then
        // Like 객체의 post 필드가 newPost로 업데이트되었는지 확인
        assertThat(like.getPost()).isEqualTo(newPost);
    }
}
