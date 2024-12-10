package com.github.jkky_98.noteJ.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
class TagTest {

    private Tag tag;
    private Post post;
    private PostTag postTag;

    @BeforeEach
    void setup() {
        // 공통적으로 사용하는 객체 초기화
        tag = Tag.builder()
                .name("Test Tag")
                .build();

        post = Post.builder()
                .title("Test Post")
                .content("Test Content")
                .build();

        postTag = PostTag.builder()
                .post(post)
                .build();
    }

    @Test
    @DisplayName("[Tag] 빌더를 통한 객체 생성 테스트")
    void tagBuilderTest() {
        // given
        String tagName = "Test Tag";

        Tag tag = Tag.builder()
                .name(tagName)
                .build();

        // then
        assertThat(tag).isNotNull();
        assertThat(tag.getName()).isEqualTo(tagName);
        assertThat(tag.getPostTags()).isNotNull();
        assertThat(tag.getPostTags()).isEmpty();
    }

    @Test
    @DisplayName("[Tag] 기본 상태 테스트")
    void tagDefaultStateTest() {
        // given
        Tag tag = Tag.builder().build();

        // then
        assertThat(tag).isNotNull();
        assertThat(tag.getName()).isNull();
        assertThat(tag.getPostTags()).isNotNull();
        assertThat(tag.getPostTags()).isEmpty();
    }

    @Test
    @DisplayName("[Tag] @Builder.Default 초기화 테스트")
    void builderDefaultInitializationTest() {
        // given
        Tag tag = Tag.builder().build();

        // then
        assertThat(tag.getPostTags()).isNotNull();
        assertThat(tag.getPostTags()).isEmpty();
    }

    @Test
    @DisplayName("[Tag] addPostTag 메서드 테스트")
    void addPostTagTest() {
        // when
        tag.addPostTag(postTag);

        // then
        // 태그에 PostTag가 추가되었는지 확인
        assertThat(tag.getPostTags()).contains(postTag);

        // PostTag의 tag 필드가 설정되었는지 확인
        assertThat(postTag.getTag()).isEqualTo(tag);
    }

    @Test
    @DisplayName("[Tag] removePostTag 메서드 테스트")
    void removePostTagTest() {
        // given
        tag.addPostTag(postTag);

        // when
        tag.removePostTag(postTag);

        // then
        // 태그에서 PostTag가 제거되었는지 확인
        assertThat(tag.getPostTags()).doesNotContain(postTag);

        // PostTag의 tag 필드가 null로 설정되었는지 확인
        assertThat(postTag.getTag()).isNull();
    }
}