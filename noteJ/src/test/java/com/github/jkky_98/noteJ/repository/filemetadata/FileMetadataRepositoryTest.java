package com.github.jkky_98.noteJ.repository.filemetadata;

import com.github.jkky_98.noteJ.domain.FileMetadata;
import com.github.jkky_98.noteJ.repository.FileMetadataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class FileMetadataRepositoryTest {

    @Autowired
    private FileMetadataRepository fileMetaDataRepository;

    private FileMetadata fileMetadata;

    @BeforeEach
    void setup() {
        fileMetadata = FileMetadata.builder()
                .originalFileName("original_file.png")
                .storedFileName("stored_file.png")
                .filePath("/uploads/images/")
                .fileType("image/png")
                .fileSize(1024L)
                .build();
    }

    @Test
    @DisplayName("[FileMetadataRepository] 파일 메타데이터 저장 테스트")
    void saveFileMetadataTest() {
        // when
        FileMetadata savedFileMetadata = fileMetaDataRepository.save(fileMetadata);
        // then
        assertThat(savedFileMetadata.getId()).isNotNull();
        assertThat(savedFileMetadata.getOriginalFileName()).isEqualTo("original_file.png");
        assertThat(savedFileMetadata.getStoredFileName()).isEqualTo("stored_file.png");
        assertThat(savedFileMetadata.getFilePath()).isEqualTo("/uploads/images/");
        assertThat(savedFileMetadata.getFileType()).isEqualTo("image/png");
        assertThat(savedFileMetadata.getFileSize()).isEqualTo(1024L);
    }

    @Test
    @DisplayName("[FileMetadataRepository] 파일 메타데이터 조회 테스트")
    void findFileMetadataByIdTest() {
        // given
        FileMetadata savedFileMetadata = fileMetaDataRepository.save(fileMetadata);

        // when
        Optional<FileMetadata> foundFileMetadata = fileMetaDataRepository.findById(savedFileMetadata.getId());

        // then
        assertThat(foundFileMetadata).isPresent();
        assertThat(foundFileMetadata.get().getOriginalFileName()).isEqualTo("original_file.png");
    }

    @Test
    @DisplayName("[FileMetadataRepository] 파일 메타데이터 삭제 테스트")
    void deleteFileMetadataTest() {
        // given
        FileMetadata savedFileMetadata = fileMetaDataRepository.save(fileMetadata);

        // when
        fileMetaDataRepository.delete(savedFileMetadata);

        // then
        Optional<FileMetadata> deletedFileMetadata = fileMetaDataRepository.findById(savedFileMetadata.getId());
        assertThat(deletedFileMetadata).isEmpty();
    }

    @Test
    @DisplayName("[FileMetadataRepository] 파일 메타데이터 업데이트 테스트")
    void updateFileMetadataTest() {
        // given
        FileMetadata savedFileMetadata = fileMetaDataRepository.save(fileMetadata);

        FileMetadata updateFileMetadata = FileMetadata.builder()
                .originalFileName("updated_file.png")
                .storedFileName("updated_stored_file.png")
                .filePath("/uploads/new_images/")
                .fileType("image/jpeg")
                .fileSize(2048L)
                .build();

        // when
        savedFileMetadata.updateFileMetadata(updateFileMetadata);
        FileMetadata updatedFileMetadata = fileMetaDataRepository.save(savedFileMetadata);

        // then
        assertThat(updatedFileMetadata.getOriginalFileName()).isEqualTo("updated_file.png");
        assertThat(updatedFileMetadata.getStoredFileName()).isEqualTo("updated_stored_file.png");
        assertThat(updatedFileMetadata.getFilePath()).isEqualTo("/uploads/new_images/");
        assertThat(updatedFileMetadata.getFileType()).isEqualTo("image/jpeg");
        assertThat(updatedFileMetadata.getFileSize()).isEqualTo(2048L);
    }
}
