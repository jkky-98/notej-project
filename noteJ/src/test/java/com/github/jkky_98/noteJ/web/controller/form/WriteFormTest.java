package com.github.jkky_98.noteJ.web.controller.form;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;

public class WriteFormTest {

    @Test
    @DisplayName("[WriteForm] 객체 생성 및 기본 상태 테스트")
    void writeFormDefaultStateTest() {
        // given
        WriteForm writeForm = new WriteForm();

        // then
        assertThat(writeForm).isNotNull();
        assertThat(writeForm.getTitle()).isNull();
        assertThat(writeForm.getTags()).isNull();
        assertThat(writeForm.getContent()).isNull();
        assertThat(writeForm.getThumbnail()).isNull();
        assertThat(writeForm.getPostSummary()).isNull();
        assertThat(writeForm.isOpen()).isFalse();
        assertThat(writeForm.getUrl()).isNull();
        assertThat(writeForm.getSeries()).isNull();
    }

    @Test
    @DisplayName("[WriteForm] 필드 값 설정 및 확인 테스트")
    void writeFormFieldAssignmentTest() {
        // given
        String title = "Test Title";
        String tags = "tag1, tag2";
        String content = "This is a test content.";
        MockMultipartFile thumbnail = new MockMultipartFile("thumbnail", "test.jpg", "image/jpeg", "file content".getBytes());
        String postSummary = "This is a test summary.";
        boolean open = true;
        String url = "http://example.com/test";
        String series = "Test Series";

        WriteForm writeForm = new WriteForm();

        // when
        writeForm.setTitle(title);
        writeForm.setTags(tags);
        writeForm.setContent(content);
        writeForm.setThumbnail(thumbnail);
        writeForm.setPostSummary(postSummary);
        writeForm.setOpen(open);
        writeForm.setUrl(url);
        writeForm.setSeries(series);

        // then
        assertThat(writeForm.getTitle()).isEqualTo(title);
        assertThat(writeForm.getTags()).isEqualTo(tags);
        assertThat(writeForm.getContent()).isEqualTo(content);
        assertThat(writeForm.getThumbnail()).isEqualTo(thumbnail);
        assertThat(writeForm.getPostSummary()).isEqualTo(postSummary);
        assertThat(writeForm.isOpen()).isEqualTo(open);
        assertThat(writeForm.getUrl()).isEqualTo(url);
        assertThat(writeForm.getSeries()).isEqualTo(series);
    }

    @Test
    @DisplayName("[WriteForm] equals 및 hashCode 테스트")
    void writeFormEqualsAndHashCodeTest() {
        // given
        MockMultipartFile thumbnail = new MockMultipartFile("thumbnail", "test.jpg", "image/jpeg", "file content".getBytes());

        WriteForm writeForm1 = new WriteForm();
        writeForm1.setTitle("Test Title");
        writeForm1.setTags("tag1, tag2");
        writeForm1.setContent("This is a test content.");
        writeForm1.setThumbnail(thumbnail);
        writeForm1.setPostSummary("This is a test summary.");
        writeForm1.setOpen(true);
        writeForm1.setUrl("http://example.com/test");
        writeForm1.setSeries("Test Series");

        WriteForm writeForm2 = new WriteForm();
        writeForm2.setTitle("Test Title");
        writeForm2.setTags("tag1, tag2");
        writeForm2.setContent("This is a test content.");
        writeForm2.setThumbnail(thumbnail);
        writeForm2.setPostSummary("This is a test summary.");
        writeForm2.setOpen(true);
        writeForm2.setUrl("http://example.com/test");
        writeForm2.setSeries("Test Series");

        // then
        assertThat(writeForm1).isEqualTo(writeForm2);
        assertThat(writeForm1.hashCode()).isEqualTo(writeForm2.hashCode());
    }
}
