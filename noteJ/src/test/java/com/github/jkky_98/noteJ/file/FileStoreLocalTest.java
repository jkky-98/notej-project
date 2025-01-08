package com.github.jkky_98.noteJ.file;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("local")
class FileStoreLocalTest {

    @Value("${file.dir}")
    private String fileDir;

    @Autowired
    private FileStoreLocal fileStoreLocal;

    @Test
    @DisplayName("[FileStoreLocal] getFullPath 성공 테스트")
    void getFullPath() {
    	// given
        // when
        String testPath = fileStoreLocal.getFullPath("test");

    	// then
        assertThat(testPath).isEqualTo(fileDir + "test");
    }


    @Test
    @DisplayName("[FileStoreLocal] storeFile 성공 테스트")
    void storeFileSuccessTest() throws IOException {
        // given
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "example.txt",
                "text/plain",
                "This is a test file content.".getBytes()
        );

        // when
        String storedFileName = fileStoreLocal.storeFile(mockFile);

        // then
        assertThat(storedFileName).matches("^[a-f0-9\\-]+\\.txt$");

        // 파일이 실제로 저장되었는지 확인
        File storedFile = new File(fileStoreLocal.getFullPath(storedFileName));
        assertThat(storedFile).exists();

        // 저장된 파일 삭제
        storedFile.delete();
    }
}
