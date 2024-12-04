package com.github.jkky_98.noteJ.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PostTagTest {

    @Test
    @DisplayName("[PostTag] 빌더를 통한 객체 생성 테스트")
    void postTagBuilderTest() {
        // given
        Post post = Post.builder()
                .title("Test Post")
                .content("This is a test post.")
                .build();

        Tag tag = Tag.builder()
                .name("Test Tag")
                .build();

        PostTag postTag = PostTag.builder()
                .post(post)
                .tag(tag)
                .build();

        // then
        assertThat(postTag).isNotNull();
        assertThat(postTag.getPost()).isEqualTo(post);
        assertThat(postTag.getTag()).isEqualTo(tag);
    }

    @Test
    @DisplayName("[PostTag] 기본 상태 테스트")
    void postTagDefaultStateTest() {
        // given
        PostTag postTag = PostTag.builder().build();

        // then
        assertThat(postTag).isNotNull();
        assertThat(postTag.getPost()).isNull();
        assertThat(postTag.getTag()).isNull();
    }
}
