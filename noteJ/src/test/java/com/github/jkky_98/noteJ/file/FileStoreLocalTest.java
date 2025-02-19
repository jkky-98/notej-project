package com.github.jkky_98.noteJ.file;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FileStoreLocalTest {

    private FileStoreLocal fileStoreLocal;

    @TempDir
    Path tempDir; // JUnit의 @TempDir을 사용하여 임시 디렉토리 생성

    @BeforeEach
    void setUp() {
        fileStoreLocal = new FileStoreLocal();
        // 테스트용 임시 폴더로 fileDir 설정 (마지막에 "/" 추가)
        ReflectionTestUtils.setField(fileStoreLocal, "fileDir", tempDir.toString() + File.separator);
    }

    @AfterEach
    void cleanup() {
        // 임시 디렉토리 내 생성된 모든 파일 삭제
        File dir = tempDir.toFile();
        if (dir.exists() && dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                file.delete();
            }
        }
    }

    @Test
    @DisplayName("[FileStoreLocal] getFullPath 메서드 테스트")
    void testGetFullPath() {
        String fileName = "sample.jpg";
        String fullPath = fileStoreLocal.getFullPath(fileName);
        assertThat(fullPath).isEqualTo(tempDir.toString() + File.separator + fileName);
    }

    @Test
    @DisplayName("[FileStoreLocal] storeFile 테스트 - 정상 JPEG 파일 저장")
    void testStoreFile_Success() throws IOException {
        // Given: 512x512 크기의 JPEG 이미지 생성
        byte[] imageBytes = createTestImageBytes(512, 512, "jpg");
        MockMultipartFile mockFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", imageBytes);

        // When
        String storedFileName = fileStoreLocal.storeFile(mockFile);
        File storedFile = new File(fileStoreLocal.getFullPath(storedFileName));

        // Then: 저장된 파일명이 null이 아니며, 파일이 실제 존재해야 함
        assertNotNull(storedFileName);
        assertTrue(storedFile.exists());
    }

    @Test
    @DisplayName("[FileStoreLocal] storeFile 테스트 - 파일 크기 초과 시 예외 발생")
    void testStoreFile_TooLargeFile_ShouldThrowException() {
        // Given: 11MB 크기의 임의 데이터 생성
        byte[] largeFile = new byte[11 * 1024 * 1024];
        MockMultipartFile mockFile = new MockMultipartFile("file", "large.jpg", "image/jpeg", largeFile);

        // When & Then
        IOException exception = assertThrows(IOException.class, () -> fileStoreLocal.storeFile(mockFile));
        assertThat(exception.getMessage()).contains("파일 크기가 너무 큽니다");
    }

    @Test
    @DisplayName("[FileStoreLocal] storeFile 테스트 - 허용되지 않은 확장자")
    void testStoreFile_InvalidExtension_ShouldThrowException() {
        // Given: 텍스트 파일 (확장자 .txt는 허용되지 않음)
        byte[] content = "dummy content".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile mockFile = new MockMultipartFile("file", "test.txt", "text/plain", content);

        // When & Then
        IOException exception = assertThrows(IOException.class, () -> fileStoreLocal.storeFile(mockFile));
        assertThat(exception.getMessage()).contains("허용되지 않은 파일 형식입니다.");
    }

    @Test
    @DisplayName("[FileStoreLocal] storeFile 테스트 - PNG 파일 리사이즈 및 JPEG 변환")
    void testStoreFile_ResizeImage_Success() throws IOException {
        // Given: 150x150 크기의 PNG 이미지 생성 (profilePicSize는 120으로 설정되어 있으므로 리사이즈됨)
        byte[] imageBytes = createTestImageBytes(150, 150, "png");
        MockMultipartFile mockFile = new MockMultipartFile("file", "test.png", "image/png", imageBytes);

        // When
        String storedFileName = fileStoreLocal.storeFile(mockFile);
        // PNG는 JPEG로 변환되므로 확장자가 .jpeg로 변경되어야 함
        assertThat(storedFileName).endsWith(".jpeg");
        File storedFile = new File(fileStoreLocal.getFullPath(storedFileName));

        // Then
        assertNotNull(storedFileName);
        assertTrue(storedFile.exists());
        BufferedImage resizedImage = ImageIO.read(storedFile);
        assertNotNull(resizedImage);
        // 이미지 크기는 profilePicSize (120x120)로 조정됨
        assertEquals(120, resizedImage.getWidth());
        assertEquals(120, resizedImage.getHeight());
    }

    @Test
    @DisplayName("[FileStoreLocal] storeFile 테스트 - 작은 이미지 (리사이즈 없이 저장)")
    void testStoreFile_AlreadySmallImage_ShouldSaveWithoutResize() throws IOException {
        // Given: 100x100 크기의 JPEG 이미지 생성 (작아서 리사이즈가 발생하지 않음)
        byte[] imageBytes = createTestImageBytes(100, 100, "jpg");
        MockMultipartFile mockFile = new MockMultipartFile("file", "small.jpg", "image/jpeg", imageBytes);

        // When
        String storedFileName = fileStoreLocal.storeFile(mockFile);
        File storedFile = new File(fileStoreLocal.getFullPath(storedFileName));

        // Then
        assertNotNull(storedFileName);
        assertTrue(storedFile.exists());
        BufferedImage savedImage = ImageIO.read(storedFile);
        assertNotNull(savedImage);
        // 리사이즈가 발생하지 않으므로 원본 크기 유지
        assertEquals(100, savedImage.getWidth());
        assertEquals(100, savedImage.getHeight());
    }

    /**
     * 테스트용 이미지 바이트 배열 생성
     * @param width 이미지 가로 크기
     * @param height 이미지 세로 크기
     * @param format 이미지 포맷 (예: "jpg", "png")
     * @return 생성된 이미지의 바이트 배열
     * @throws IOException
     */
    private static byte[] createTestImageBytes(int width, int height, String format) throws IOException {
        BufferedImage image = new BufferedImage(width, height,
                format.equalsIgnoreCase("png") ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);

        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.BLUE);
        graphics.fillRect(0, 0, width, height);
        graphics.setColor(Color.WHITE);
        graphics.drawString("Test Image", width / 4, height / 2);
        graphics.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, format, baos);
        return baos.toByteArray();
    }
}