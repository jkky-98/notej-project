package com.github.jkky_98.noteJ.file;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Component
@Profile("!local")
public class FileStoreS3 implements FileStore {

    private final AmazonS3 amazonS3;
    @Value("${cloud.aws.s3.bucket}")
    private String s3BucketName;

    // S3 클라이언트 의존성 주입
    public FileStoreS3(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    @Override
    public String storeFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return null;
        }

        String storeFileName = createStoredFileName(file.getOriginalFilename());
        // 파일을 S3에 업로드
        uploadToS3(file, storeFileName);

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
    private void uploadToS3(MultipartFile file, String storedFileName) throws IOException {
        try (InputStream inputStream = file.getInputStream()) {
            // S3에 파일 업로드 요청
            amazonS3.putObject(new PutObjectRequest(s3BucketName, storedFileName, inputStream, null));
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

}