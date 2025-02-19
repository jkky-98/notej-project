package com.github.jkky_98.noteJ.domain;

import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.domain.user.UserDesc;
import com.github.jkky_98.noteJ.domain.user.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CommentTest {

    @Test
    @DisplayName("[Comment] 빌더를 통한 객체 생성 테스트")
    void commentBuilderTest() {
        // given
        User testUser = createTestUser();
        Post testPost = createTestPost(testUser);

        Comment comment = Comment.builder()
                .content("테스트 댓글")
                .post(testPost)
                .user(testUser)
                .build();

        // then
        assertThat(comment.getContent()).isEqualTo("테스트 댓글");
        assertThat(comment.getPost()).isEqualTo(testPost);
        assertThat(comment.getUser()).isEqualTo(testUser);
        // Self-referencing 필드는 기본적으로 null 이어야 함
        assertThat(comment.getParent()).isNull();
    }


    @Test
    @DisplayName("[Comment] 기본 상태 테스트")
    void commentDefaultStateTest() {
        // given
        Comment comment = Comment.builder()
                .build();

        // then
        assertThat(comment).isNotNull();
        assertThat(comment.getContent()).isNull();
        assertThat(comment.getPost()).isNull();
        assertThat(comment.getUser()).isNull();
        assertThat(comment.getParent()).isNull();
    }

    @Test
    @DisplayName("[Comment] Builder.Default 초기화 테스트 (childrens 리스트)")
    void builderDefaultInitializationTest() {
        // given
        User testUser = createTestUser();
        Post testPost = createTestPost(testUser);

        Comment comment = Comment.builder()
                .content("테스트 댓글")
                .post(testPost)
                .user(testUser)
                .build();

        // then
        assertThat(comment.getChildrens()).isNotNull();
        assertThat(comment.getChildrens().isEmpty()).isTrue();
    }

    @Test
    @DisplayName("[Comment] updatePost 메서드 테스트")
    void updatePostTest() {
        // given
        User testUser = createTestUser();
        Post oldPost = createTestPost(testUser);
        Post newPost = Post.builder()
                .title("테스트 포스트 제목2")
                .content("테스트 포스트 내용")
                .user(testUser)
                .build();

        Comment comment = Comment.builder()
                .content("업데이트 테스트 댓글")
                .post(oldPost)
                .user(testUser)
                .build();

        // when
        comment.updatePost(newPost);

        // then
        assertThat(comment.getPost()).isEqualTo(newPost);
    }

    // 테스트용 User 생성 (UserTest의 createTestUser 참고)
    private User createTestUser() {
        UserDesc userDesc = UserDesc.builder()
                .description("테스트 유저 설명")
                .blogTitle("테스트 블로그 제목")
                .build();

        return User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .userRole(UserRole.USER)
                .userDesc(userDesc)
                .build();
    }

    // 테스트용 Post 생성 (간단한 필드만 설정)
    private Post createTestPost(User user) {
        return Post.builder()
                .title("테스트 포스트 제목")
                .content("테스트 포스트 내용")
                .user(user)
                .build();
    }
}
