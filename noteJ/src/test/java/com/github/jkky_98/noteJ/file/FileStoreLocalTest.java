package com.github.jkky_98.noteJ.file;

import org.junit.jupiter.api.BeforeEach;
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
        // fileDir을 테스트용 임시 폴더로 설정
        ReflectionTestUtils.setField(fileStoreLocal, "fileDir", tempDir.toString() + "/");
    }

    @Test
    void testStoreFile_Success() throws IOException {
        // Given
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                createTestImageBytes(512, 512, "jpg") // 512x512 사이즈의 이미지 생성
        );

        // When
        String storedFileName = fileStoreLocal.storeFile(mockFile);
        File storedFile = new File(tempDir.toString(), storedFileName);

        // Then
        assertNotNull(storedFileName);
        assertTrue(storedFile.exists()); // 파일이 실제로 저장되었는지 확인
    }
//
    @Test
    void testStoreFile_TooLargeFile_ShouldThrowException() {
        // Given: 11MB 크기의 파일 생성
        byte[] largeFile = new byte[11 * 1024 * 1024];
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "large.jpg",
                "image/jpeg",
                largeFile
        );

        // When & Then
        IOException exception = assertThrows(IOException.class, () -> fileStoreLocal.storeFile(mockFile));
        assertThat(exception.getMessage()).contains("파일 크기가 너무 큽니다.");
    }

    @Test
    void testStoreFile_InvalidExtension_ShouldThrowException() {
        // Given
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Hello, this is a test file.".getBytes()
        );

        // When & Then
        IOException exception = assertThrows(IOException.class, () -> fileStoreLocal.storeFile(mockFile));
        assertThat(exception.getMessage()).contains("허용되지 않은 파일 형식입니다.");
    }

    @Test
    void testStoreFile_ResizeImage_Success() throws IOException {
        // Given: 200x200 이미지를 업로드
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "largeImage.png",
                "image/png",
                createTestImageBytes(150, 150, "png")
        );

        // When
        String storedFileName = fileStoreLocal.storeFile(mockFile);

        // 🔹 실제 저장된 파일 경로를 `getFullPath()`로 확인
        File storedFile = new File(fileStoreLocal.getFullPath(storedFileName));

        // 디버깅 로그 추가
        System.out.println("Stored File Name: " + storedFileName);
        System.out.println("Expected File Path: " + storedFile.getAbsolutePath());

        // Then
        assertNotNull(storedFileName);
        assertTrue(storedFile.exists());  // ✅ 올바른 경로에서 파일 확인

        // 저장된 이미지 확인 (WebP 변환 대신 JPG 변환 확인)
        BufferedImage resizedImage = ImageIO.read(storedFile);
        assertNotNull(resizedImage);
        assertEquals(120, resizedImage.getWidth());
        assertEquals(120, resizedImage.getHeight());
    }

    @Test
    void testStoreFile_AlreadySmallImage_ShouldSaveWithoutResize() throws IOException {
        // Given: 100x100 크기의 작은 이미지
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "smallImage.jpg",
                "image/jpeg",
                createTestImageBytes(100, 100, "jpg")
        );

        // When
        String storedFileName = fileStoreLocal.storeFile(mockFile);
        File storedFile = new File(tempDir.toString(), storedFileName);

        // Then
        assertNotNull(storedFileName);
        assertTrue(storedFile.exists());

        // 저장된 이미지 확인 (리사이징이 안 되었는지 체크)
        BufferedImage originalImage = ImageIO.read(storedFile);
        assertNotNull(originalImage);
        assertEquals(100, originalImage.getWidth());
        assertEquals(100, originalImage.getHeight());
    }

    public static byte[] createTestImageBytes(int width, int height, String format) throws IOException {
        // BufferedImage 생성 (RGB 타입)
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // 그래픽 객체를 가져와서 색상을 채움 (샘플 이미지)
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.BLUE); // 배경 색상 설정
        graphics.fillRect(0, 0, width, height);
        graphics.setColor(Color.WHITE);
        graphics.drawString("Test Image", width / 4, height / 2); // 텍스트 추가
        graphics.dispose();

        // 바이트 배열로 변환
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, format, baos);
        return baos.toByteArray();
    }

}