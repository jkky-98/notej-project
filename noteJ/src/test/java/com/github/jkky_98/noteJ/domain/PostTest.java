package com.github.jkky_98.noteJ.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PostTest {

    @Test
    @DisplayName("[Post] 빌더를 통한 객체 생성 테스트")
    void postBuilderTest() {
        // given
        String title = "Test Title";
        String content = "This is the content of the post.";
        String thumbnail = "https://example.com/thumbnail.jpg";
        Boolean isPrivate = true;

        Post post = Post.builder()
                .title(title)
                .content(content)
                .thumbnail(thumbnail)
                .writable(isPrivate)
                .build();

        // then
        assertThat(post).isNotNull();
        assertThat(post.getTitle()).isEqualTo(title);
        assertThat(post.getContent()).isEqualTo(content);
        assertThat(post.getThumbnail()).isEqualTo(thumbnail);
        assertThat(post.getWritable()).isEqualTo(isPrivate);
        assertThat(post.getSeries()).isNull(); // 연관관계는 설정하지 않았으므로 null
        assertThat(post.getUser()).isNull();
    }

    @Test
    @DisplayName("[Post] 기본 상태 테스트")
    void postDefaultStateTest() {
        // given
        Post post = Post.builder().build();

        // then
        assertThat(post).isNotNull();
        assertThat(post.getTitle()).isNull();
        assertThat(post.getContent()).isNull();
        assertThat(post.getThumbnail()).isNull();
        assertThat(post.getWritable()).isNull();
        assertThat(post.getSeries()).isNull();
        assertThat(post.getUser()).isNull();
        assertThat(post.getComments()).isNotNull();
        assertThat(post.getLikes()).isNotNull();
        assertThat(post.getPostTags()).isNotNull();
    }

    @Test
    @DisplayName("[Post] @Builder.Default 초기화 테스트")
    void builderDefaultInitializationTest() {
        // given
        Post post = Post.builder().build();

        // then
        assertThat(post.getComments()).isNotNull();
        assertThat(post.getComments()).isEmpty();

        assertThat(post.getLikes()).isNotNull();
        assertThat(post.getLikes()).isEmpty();

        assertThat(post.getPostTags()).isNotNull();
        assertThat(post.getPostTags()).isEmpty();
    }
}
