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
}
