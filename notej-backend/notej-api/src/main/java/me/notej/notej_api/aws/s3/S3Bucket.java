package me.notej.notej_api.aws.s3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3Bucket {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * S3에 파일 업로드
     */
    public String upload(MultipartFile file, String dirName) {
        // 1. 파일 유효성 검사
        validateFile(file);

        // 2. 파일 이름 생성 (UUID로 고유하게)
        String fileName = createFileName(file.getOriginalFilename(), dirName);

        try {
            // 3. PutObjectRequest 생성
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            // 4. 파일 업로드 (Request Body)
            RequestBody requestBody = RequestBody.fromInputStream(file.getInputStream(), file.getSize());

            // 5. S3에 업로드 실행
            s3Client.putObject(putObjectRequest, requestBody);

            // 6. 업로드된 파일의 S3 URL 반환
            return fileName;

        } catch (IOException e) {
            log.error("S3 파일 업로드 중 IO 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("파일 업로드 중 오류 발생", e);
        } catch (S3Exception e) { // S3 관련 에러 처리
            log.error("S3 서비스 오류 발생: {}", e.awsErrorDetails().errorMessage(), e);
            throw new RuntimeException("S3 서비스 오류: " + e.awsErrorDetails().errorMessage(), e);
        }
    }

    /**
     * S3에서 파일 삭제
     */
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            log.warn("삭제할 파일 URL이 null 또는 비어있습니다.");
            return;
        }

        try {
            // URL에서 키 추출 (https://bucket.s3.region.amazonaws.com/profile-images/uuid.jpg)
            // 더 안전하게 키 추출
            String key = getKeyFromS3Url(fileUrl, bucket);

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("S3 파일 삭제 성공: {}", fileUrl);

        } catch (S3Exception e) {
            log.warn("S3 파일 삭제 실패 - S3 오류: {}", e.awsErrorDetails().errorMessage(), e);
            // 파일이 없거나 삭제 권한이 없는 경우 등은 무시 (파일이 없을 땐 NoSuchKeyException)
        } catch (Exception e) {
            log.warn("S3 파일 삭제 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
        }
    }

    /**
     * S3 URL에서 객체 키(경로) 추출 유틸리티
     * 안전성을 높이기 위해 좀 더 정교하게 구현
     */
    private String getKeyFromS3Url(String fileUrl, String bucketName) {
        String bucketDomain = ".s3." + s3Client.serviceClientConfiguration().region().id() + ".amazonaws.com/";
        String oldBucketDomain = ".s3.amazonaws.com/"; // 이전 버전 또는 us-east-1의 경우

        // s3 Region-specific domain (예: bucket.s3.ap-northeast-2.amazonaws.com)
        int index = fileUrl.indexOf(bucketName + bucketDomain);
        if (index > -1) {
            return fileUrl.substring(index + (bucketName + bucketDomain).length());
        }

        // s3 global domain (us-east-1 또는 virtual-hosted-style)
        index = fileUrl.indexOf(bucketName + oldBucketDomain);
        if (index > -1) {
            return fileUrl.substring(index + (bucketName + oldBucketDomain).length());
        }

        // path-style access (legacy, not recommended)
        index = fileUrl.indexOf("/" + bucketName + "/");
        if (index > -1) {
            return fileUrl.substring(index + ("/" + bucketName + "/").length()); // /bucket/ 길이만큼 건너뛰기
        }

        // 만약 URL 형식이 예상과 다르거나 bucketName이 포함되지 않은 경우
        log.warn("S3 URL에서 키 추출 실패: {} (버킷명: {})", fileUrl, bucketName);
        throw new IllegalArgumentException("올바른 S3 URL 형식이 아닙니다: " + fileUrl);
    }

    /**
     * 파일 이름 생성 (UUID 사용)
     */
    private String createFileName(String originalFileName, String dirName) {
        String uuid = UUID.randomUUID().toString();
        String ext = extractExtension(originalFileName);
        // dirName이 비어있지 않은 경우에만 경로 추가
        return (dirName != null && !dirName.isEmpty() ? dirName + "/" : "") + uuid + "." + ext;
    }

    /**
     * 파일 확장자 추출
     */
    private String extractExtension(String originalFileName) {
        int pos = originalFileName.lastIndexOf(".");
        if (pos == -1 || pos == originalFileName.length() - 1) { // 확장자가 없거나 .으로 끝나는 경우
            throw new IllegalArgumentException("유효하지 않은 파일 이름입니다: " + originalFileName);
        }
        return originalFileName.substring(pos + 1);
    }

    /**
     * 파일 유효성 검사
     */
    private void validateFile(MultipartFile file) {
        // 빈 파일인지 확인
        if (file.isEmpty()) {
            throw new IllegalArgumentException("빈 파일입니다.");
        }

        // 이미지 파일인지 확인 (프론트에서 해도 백엔드에서 다시 검증)
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
        }

        // 파일 크기 확인 (10MB 제한)
        long maxSize = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("파일 크기는 10MB 이하여야 합니다.");
        }
    }

    /**
     * Signed URL 생성
     * @param objectKey
     * @param expiration
     * @return
     */
    public String generateSignedUrl(String objectKey, Duration expiration) {
        if (objectKey == null || objectKey.isEmpty()) {
            return null;
        }

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(expiration)
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
        return presignedRequest.url().toString();
    }
}
