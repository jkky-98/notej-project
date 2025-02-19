package com.github.jkky_98.noteJ.domain;

import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.domain.user.UserDesc;
import com.github.jkky_98.noteJ.domain.user.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LikeTest {
    @Test
    @DisplayName("[Like] Builder 패턴 테스트")
    void likeBuilderTest() {
        // given: 테스트용 User, Post, 그리고 userGetLike 생성
        User user = createTestUser("user1", "user1@example.com");
        User userGetLike = createTestUser("user2", "user2@example.com");
        Post post = createTestPost(user);

        // when: Like 엔티티 빌더를 사용해 객체 생성
        Like like = Like.builder()
                .post(post)
                .user(user)
                .userGetLike(userGetLike)
                .build();

        // then: 각 필드가 올바르게 설정되었는지 검증
        assertThat(like.getPost()).isEqualTo(post);
        assertThat(like.getUser()).isEqualTo(user);
        assertThat(like.getUserGetLike()).isEqualTo(userGetLike);
    }

    @Test
    @DisplayName("[Like] 기본 상태 테스트")
    void likeDefaultStateTest() {
        // given: 기본 빌더로 Like 객체 생성 (아무 필드도 설정하지 않음)
        Like like = Like.builder().build();

        // then: 기본 상태 값 검증
        // id는 아직 영속화되지 않았으므로 null
        // 연관관계 필드들(post, user, userGetLike)도 기본값 null이어야 함
        assertThat(like.getId()).isNull();
        assertThat(like.getPost()).isNull();
        assertThat(like.getUser()).isNull();
        assertThat(like.getUserGetLike()).isNull();
    }

    @Test
    @DisplayName("[Like] updatePost 메서드 테스트")
    void updatePostTest() {
        // given: 테스트용 User, Post 생성
        User user = createTestUser("user1", "user1@example.com");
        User userGetLike = createTestUser("user2", "user2@example.com");
        Post oldPost = createTestPost(user);
        Post newPost = Post.builder()
                .title("테스트 포스트 제목2")
                .content("테스트 포스트 내용2")
                .user(user)
                .build();

        Like like = Like.builder()
                .post(oldPost)
                .user(user)
                .userGetLike(userGetLike)
                .build();

        // when: updatePost 메서드 호출하여 포스트 변경
        like.updatePost(newPost);

        // then: 포스트가 새로운 포스트로 업데이트 되었는지 검증
        assertThat(like.getPost()).isEqualTo(newPost);
    }

    // 테스트용 User 객체 생성 헬퍼 메서드
    private User createTestUser(String username, String email) {
        UserDesc userDesc = UserDesc.builder()
                .description("테스트 유저 설명")
                .blogTitle("테스트 블로그")
                .build();

        return User.builder()
                .username(username)
                .email(email)
                .password("password123")
                .userRole(UserRole.USER)
                .userDesc(userDesc)
                .build();
    }

    // 테스트용 Post 객체 생성 헬퍼 메서드
    private Post createTestPost(User user) {
        return Post.builder()
                .title("테스트 포스트 제목")
                .content("테스트 포스트 내용")
                .user(user)
                .build();
    }
}
