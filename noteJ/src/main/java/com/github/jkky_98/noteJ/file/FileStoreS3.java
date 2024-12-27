package com.github.jkky_98.noteJ.file;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.github.jkky_98.noteJ.domain.FileMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

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

    /**
     * S3에 파일을 업로드하고, 파일 메타데이터를 반환합니다.
     */
    public FileMetadata storeFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return null;
        }

        // 파일 메타데이터 생성
        FileMetadata fileMetadata = FileMetadata.builder()
                .originalFileName(file.getOriginalFilename())
                .storedFileName(createStoredFileName(file.getOriginalFilename()))
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .build();

        // 파일을 S3에 업로드
        uploadToS3(file, fileMetadata.getStoredFileName());

        return fileMetadata;
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