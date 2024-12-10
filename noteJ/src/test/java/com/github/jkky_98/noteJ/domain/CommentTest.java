package com.github.jkky_98.noteJ.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CommentTest {

    @Test
    @DisplayName("[Comment] 빌더를 통한 객체 생성 테스트")
    void commentBuilderTest() {
        // given
        String testContent = "This is a comment.";

        Comment comment = Comment.builder()
                .content(testContent)
                .build();

        // then
        assertThat(comment).isNotNull();
        assertThat(comment.getContent()).isEqualTo(testContent);
        assertThat(comment.getPost()).isNull(); // 연관관계는 설정하지 않았으므로 null
        assertThat(comment.getUser()).isNull();
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
    @DisplayName("[Comment] updatePost 메서드 테스트")
    void updatePostTest() {
        // given
        Post oldPost = Post.builder()
                .title("Old Post")
                .content("Content of old post")
                .build();

        Post newPost = Post.builder()
                .title("New Post")
                .content("Content of new post")
                .build();

        Comment comment = Comment.builder()
                .content("This is a test comment")
                .post(oldPost)
                .build();

        // when
        comment.updatePost(newPost);

        // then
        // comment의 post 필드가 newPost로 업데이트되었는지 확인
        assertThat(comment.getPost()).isEqualTo(newPost);
    }
}
