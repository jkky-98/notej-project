package com.github.jkky_98.noteJ.domain;

import com.github.jkky_98.noteJ.domain.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PostFileTest {

    @Test
    @DisplayName("[PostFile] 빌더 생성 테스트")
    void postFileBuilderCreationTest() {
        // given: 테스트용 Post 객체 생성
        User testUser = createTestUser("testuser", "test@example.com");
        Post post = createTestPost(testUser);
        String url = "http://example.com/file.jpg";

        // when: 빌더를 사용하여 PostFile 객체 생성
        PostFile postFile = PostFile.builder()
                .post(post)
                .url(url)
                .build();

        // then: 각 필드가 올바르게 설정되었는지 검증
        assertThat(postFile.getPost()).isEqualTo(post);
        assertThat(postFile.getUrl()).isEqualTo(url);
    }

    @Test
    @DisplayName("[PostFile] 기본 상태 테스트")
    void postFileDefaultStateTest() {
        // given: 아무 필드도 설정하지 않고 빌더로 PostFile 객체 생성
        PostFile postFile = PostFile.builder().build();

        // then: 기본 상태 값 검증
        // id는 영속화 전이므로 null이어야 함
        assertThat(postFile.getId()).isNull();
        // post와 url은 설정하지 않았으므로 null이어야 함
        assertThat(postFile.getPost()).isNull();
        assertThat(postFile.getUrl()).isNull();
    }

    // 헬퍼 메서드: 테스트용 User 객체 생성
    private User createTestUser(String username, String email) {
        // 간단하게 User 객체를 빌더로 생성 (필요한 필드만 설정)
        return User.builder()
                .username(username)
                .email(email)
                .password("password123")
                .build();
    }

    // 헬퍼 메서드: 테스트용 Post 객체 생성
    private Post createTestPost(User user) {
        // 간단하게 Post 객체를 빌더로 생성 (필요한 필드만 설정)
        return Post.builder()
                .title("Test Post Title")
                .content("Test Post Content")
                .build();
    }
}
