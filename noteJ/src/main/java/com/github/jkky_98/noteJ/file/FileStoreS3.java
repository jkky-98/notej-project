package com.github.jkky_98.noteJ.file;

import com.amazonaws.services.s3.AmazonS3;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
@Profile("!local")
public class FileStoreS3 implements FileStore {

    private final AmazonS3 amazonS3;
    @Value("${cloud.aws.s3.bucket}")
    private String s3BucketName;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "webp");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB 제한
    private static int profilePicSize = 120;

    // S3 클라이언트 의존성 주입
    public FileStoreS3(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    @Override
    public String storeFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("파일이 비어 있습니다.");
        }

        // 파일 크기 제한 체크
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IOException("파일 크기가 너무 큽니다. (최대 10MB)");
        }

        // 파일 확장자 체크
        if (!isAllowedExtension(file.getOriginalFilename())) {
            throw new IOException("허용되지 않은 파일 형식입니다.");
        }

        // 저장될 파일명 생성 (UUID 적용)
        String storeFileName = createStoredFileName(file.getOriginalFilename());

        // 이미지 크기 확인 후 리사이징할지 결정
        BufferedImage image = ImageIO.read(file.getInputStream());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        if (image.getWidth() > profilePicSize || image.getHeight() > profilePicSize) {
            // 리사이징 및 WebP 변환
            Thumbnails.of(image)
                    .size(profilePicSize, profilePicSize)
                    .outputFormat("jpg")
                    .toOutputStream(outputStream);
        } else {
            // 원본 그대로 업로드
            file.getInputStream().transferTo(outputStream);
        }

        // S3 업로드
        uploadToS3(outputStream.toByteArray(), storeFileName);

        return storeFileName;
    }

    @Override
    public String getFullPath(String fileName) {
        return "";
    }

    @Override
    public String deleteFile(String thumbnailDeleted) {
        try {
            // S3에서 파일 삭제
            amazonS3.deleteObject(s3BucketName, thumbnailDeleted);
            return thumbnailDeleted;
        } catch (Exception e) {
            // 삭제 실패 시 예외 처리
            throw new RuntimeException("Failed to delete the file: " + thumbnailDeleted, e);
        }
    }


    /**
     * S3에 파일을 업로드
     */
    private void uploadToS3(byte[] fileBytes, String storedFileName) throws IOException {

        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(fileBytes)) {
            // S3에 파일 업로드 요청
            amazonS3.putObject(s3BucketName, storedFileName, byteArrayInputStream, null);
        }
    }

    /**
     * 저장할 파일명 생성 (UUID + 확장자)
     */
    private String createStoredFileName(String originalFilename) {
        String ext = extractExt(originalFilename);
        String uuid = UUID.randomUUID().toString();
        return uuid + "." + ext;
    }

    /**
     * 파일 확장자 추출
     */
    private String extractExt(String originalFilename) {
        int pos = originalFilename.lastIndexOf(".");
        return originalFilename.substring(pos + 1);
    }

    private boolean isAllowedExtension(String filename) {
        String ext = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        return ALLOWED_EXTENSIONS.contains(ext);
    }
}