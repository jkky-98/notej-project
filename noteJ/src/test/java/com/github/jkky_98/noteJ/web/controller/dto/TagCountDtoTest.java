package com.github.jkky_98.noteJ.web.controller.dto;


import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TagCountDtoTest {

    @Test
    @DisplayName("[TagCountDto] 객체 생성 및 값 확인 테스트")
    void tagCountDtoCreationTest() {
        // given
        String tagName = "Test Tag";
        Long count = 5L;

        // when
        TagCountDto tagCountDto = new TagCountDto(tagName, count);
        // then
        assertThat(tagCountDto).isNotNull();
        assertThat(tagCountDto.getTagName()).isEqualTo(tagName);
        assertThat(tagCountDto.getCount()).isEqualTo(count);
    }

    @Test
    @DisplayName("[TagCountDto] equals 및 hashCode 테스트")
    void tagCountDtoEqualsAndHashCodeTest() {
        // given
        TagCountDto tagCountDto1 = new TagCountDto("Test Tag", 5L);
        TagCountDto tagCountDto2 = new TagCountDto("Test Tag", 5L);

        // then
        assertThat(tagCountDto1).isEqualTo(tagCountDto2);
        assertThat(tagCountDto1.hashCode()).isEqualTo(tagCountDto2.hashCode());
    }

    @Test
    @DisplayName("[TagCountDto] 값 설정 및 수정 테스트")
    void tagCountDtoValueModificationTest() {
        // given
        TagCountDto tagCountDto = new TagCountDto("Test Tag", 5L);

        // when
        tagCountDto.setTagName("Updated Tag");
        tagCountDto.setCount(10L);

        // then
        assertThat(tagCountDto.getTagName()).isEqualTo("Updated Tag");
        assertThat(tagCountDto.getCount()).isEqualTo(10L);
    }
}
