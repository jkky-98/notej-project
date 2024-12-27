package com.github.jkky_98.noteJ.file;

import com.github.jkky_98.noteJ.domain.FileMetadata;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class FileStoreLocalTest {

    @Value("${file.dir}")
    private String fileDir;

    @Autowired
    private FileStoreLocal fileStoreLocal;

    @Test
    @DisplayName("[FileStore] getFullPath 성공 테스트")
    void getFullPath() {
    	// given
        // when
        String testPath = fileStoreLocal.getFullPath("test");

    	// then
        assertThat(testPath).isEqualTo(fileDir + "test");
    }


    @Test
    @DisplayName("[FileStore] storeFile 성공 테스트")
    void storeFileSuccessTest() throws IOException {
        // given
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "example.txt",
                "text/plain",
                "This is a test file content.".getBytes()
        );

        // when
        FileMetadata fileMetadata = fileStoreLocal.storeFile(mockFile);

        // then
        assertThat(fileMetadata).isNotNull();
        assertThat(fileMetadata.getOriginalFileName()).isEqualTo("example.txt");
        assertThat(fileMetadata.getFileType()).isEqualTo("text/plain");
        assertThat(fileMetadata.getFileSize()).isEqualTo(mockFile.getSize());
        assertThat(fileMetadata.getStoredFileName()).matches("^[a-f0-9\\-]+\\.txt$");

        // 파일이 실제로 저장되었는지 확인
        File storedFile = new File(fileStoreLocal.getFullPath(fileMetadata.getStoredFileName()));
        assertThat(storedFile).exists();

        // 저장된 파일 삭제
        storedFile.delete();
    }

    @Test
    @DisplayName("[FileStore] storeFile 빈 파일 테스트")
    void storeFileEmptyFileTest() throws IOException {
        // given
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "",
                "text/plain",
                new byte[0]
        );

        // when
        FileMetadata fileMetadata = fileStoreLocal.storeFile(emptyFile);

        // then
        assertThat(fileMetadata).isNull();
    }
}
