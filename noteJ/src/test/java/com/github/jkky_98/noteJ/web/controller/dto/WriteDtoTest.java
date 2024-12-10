package com.github.jkky_98.noteJ.web.controller.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class WriteDtoTest {

    @Test
    @DisplayName("[WriteDto] 객체 생성 및 기본 상태 테스트")
    void writeDtoCreationAndDefaultStateTest() {
        // given
        WriteDto writeDto = new WriteDto();

        // then
        assertThat(writeDto).isNotNull();
        assertThat(writeDto.getSeriesNameList()).isNotNull();
        assertThat(writeDto.getSeriesNameList()).isEmpty();
    }

    @Test
    @DisplayName("[WriteDto] 시리즈 이름 추가 및 확인 테스트")
    void writeDtoAddSeriesNameTest() {
        // given
        WriteDto writeDto = new WriteDto();

        // when
        writeDto.getSeriesNameList().add("Series 1");
        writeDto.getSeriesNameList().add("Series 2");

        // then
        assertThat(writeDto.getSeriesNameList()).containsExactly("Series 1", "Series 2");
    }

    @Test
    @DisplayName("[WriteDto] 시리즈 이름 리스트 설정 테스트")
    void writeDtoSetSeriesNameListTest() {
        // given
        WriteDto writeDto = new WriteDto();

        // when
        writeDto.setSeriesNameList(Arrays.asList("Series A", "Series B", "Series C"));

        // then
        assertThat(writeDto.getSeriesNameList()).containsExactly("Series A", "Series B", "Series C");
    }

    @Test
    @DisplayName("[WriteDto] equals 및 hashCode 테스트")
    void writeDtoEqualsAndHashCodeTest() {
        // given
        WriteDto writeDto1 = new WriteDto();
        writeDto1.setSeriesNameList(Arrays.asList("Series X", "Series Y"));

        WriteDto writeDto2 = new WriteDto();
        writeDto2.setSeriesNameList(Arrays.asList("Series X", "Series Y"));

        // then
        assertThat(writeDto1).isEqualTo(writeDto2);
        assertThat(writeDto1.hashCode()).isEqualTo(writeDto2.hashCode());
    }
}
