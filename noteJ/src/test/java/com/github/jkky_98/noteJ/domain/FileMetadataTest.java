package com.github.jkky_98.noteJ.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FileMetadataTest {

    @Test
    @DisplayName("[FileMetadata] 빌더를 통한 객체 생성 테스트")
    void fileMetadataBuilderTest() {
        // given
        String originalFileName = "image.png";
        String storedFileName = "abc123.png";
        String filePath = "/uploads/images/";
        String fileType = "image/png";
        Long fileSize = 12345L;

        FileMetadata fileMetadata = FileMetadata.builder()
                .originalFileName(originalFileName)
                .storedFileName(storedFileName)
                .filePath(filePath)
                .fileType(fileType)
                .fileSize(fileSize)
                .build();

        // then
        assertThat(fileMetadata).isNotNull();
        assertThat(fileMetadata.getOriginalFileName()).isEqualTo(originalFileName);
        assertThat(fileMetadata.getStoredFileName()).isEqualTo(storedFileName);
        assertThat(fileMetadata.getFilePath()).isEqualTo(filePath);
        assertThat(fileMetadata.getFileType()).isEqualTo(fileType);
        assertThat(fileMetadata.getFileSize()).isEqualTo(fileSize);
        assertThat(fileMetadata.getUserDesc()).isNull(); // 연관관계는 설정하지 않았으므로 null
    }

    @Test
    @DisplayName("[FileMetadata] 기본 상태 테스트")
    void fileMetadataDefaultStateTest() {
        // given
        FileMetadata fileMetadata = FileMetadata.builder().build();

        // then
        assertThat(fileMetadata).isNotNull();
        assertThat(fileMetadata.getOriginalFileName()).isNull();
        assertThat(fileMetadata.getStoredFileName()).isNull();
        assertThat(fileMetadata.getFilePath()).isNull();
        assertThat(fileMetadata.getFileType()).isNull();
        assertThat(fileMetadata.getFileSize()).isNull();
        assertThat(fileMetadata.getUserDesc()).isNull();
    }

    @Test
    @DisplayName("[FileMetadata] updateFileMetadata 메서드 테스트")
    void updateFileMetadataTest() {
        // given: 기존 FileMetadata 객체 생성
        FileMetadata originalFileMetadata = FileMetadata.builder()
                .originalFileName("original_image.png")
                .storedFileName("stored_image.png")
                .filePath("/uploads/original/")
                .fileType("image/png")
                .fileSize(1024L)
                .build();

        // 업데이트할 FileMetadata 객체 생성
        FileMetadata updateFileMetadata = FileMetadata.builder()
                .originalFileName("updated_image.png")
                .storedFileName("updated_stored_image.png")
                .filePath("/uploads/updated/")
                .fileType("image/jpeg")
                .fileSize(2048L)
                .build();

        // when: 업데이트 메서드 호출
        originalFileMetadata.updateFileMetadata(updateFileMetadata);

        // then: 필드가 업데이트되었는지 검증
        assertThat(originalFileMetadata.getOriginalFileName()).isEqualTo("updated_image.png");
        assertThat(originalFileMetadata.getStoredFileName()).isEqualTo("updated_stored_image.png");
        assertThat(originalFileMetadata.getFilePath()).isEqualTo("/uploads/updated/");
        assertThat(originalFileMetadata.getFileType()).isEqualTo("image/jpeg");
        assertThat(originalFileMetadata.getFileSize()).isEqualTo(2048L);
    }
}
