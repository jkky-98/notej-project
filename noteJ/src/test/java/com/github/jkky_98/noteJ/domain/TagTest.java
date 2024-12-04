package com.github.jkky_98.noteJ.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TagTest {

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
}
