package me.notej.notej_api.aws.s3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

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
    public String createImage(MultipartFile file, String dirName) {
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
     * S3에서 이미지를 byte[] 형태로 가져오는 메서드
     * @param s3Key 가져올 파일의 S3 키
     * @return 파일 데이터를 담은 byte 배열
     */
    public byte[] getBytesFromObject(String s3Key) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(s3Key)
                    .build();

            ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);

            // InputStream을 byte[]로 변환
            return s3Object.readAllBytes();

        } catch (NoSuchKeyException e) {
            log.warn("S3 객체를 찾을 수 없음: {}", s3Key);
            throw new IllegalArgumentException("요청한 이미지를 찾을 수 없습니다: " + s3Key);
        } catch (S3Exception e) {
            log.error("S3에서 이미지 다운로드 중 오류 발생: {}", e.awsErrorDetails().errorMessage(), e);
            throw new RuntimeException("S3에서 이미지를 가져오는 중 오류 발생: " + e.getMessage());
        } catch (IOException e) {
            log.error("이미지 데이터 읽기 중 IO 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("이미지 데이터를 읽는 중 오류 발생: " + e.getMessage());
        }
    }
    /**
     * S3 객체 키로부터 전체 URL을 생성합니다.
     * @param s3Key S3 객체 키 (경로)
     * @return 완전한 S3 URL
     */
    public String getObjectUrlFromKey(String s3Key) {
        if (s3Key == null || s3Key.isEmpty()) {
            throw new IllegalArgumentException("S3 키는 null이거나 비어있을 수 없습니다.");
        }

        // 리전 정보를 S3 클라이언트에서 동적으로 가져옴
        String region = s3Client.serviceClientConfiguration().region().id();

        // URL 형식: https://{bucket}.s3.{region}.amazonaws.com/{key}
        return String.format("https://%s.s3.%s.amazonaws.com/%s",
                bucket,
                region,
                s3Key);
    }

    /**
     * S3 URL에서 객체 키(경로) 추출 유틸리티
     * 안전성을 높이기 위해 좀 더 정교하게 구현
     */
    private String getObjectKeyFromS3Url(String fileUrl, String bucketName) {
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
    public String extractSignedUrl(String objectKey, Duration expiration) {
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

    /**
     * S3에 파일 업로드 및 "Status: delete" 태그 추가
     * - 임시 파일로 분류되어 1일 후 삭제 규칙에 포함됩니다.
     * @param file 업로드할 파일
     * @param dirName S3 내부에 생성할 디렉토리 경로 (예: "posts/username")
     * @return S3에 저장된 파일의 키 (경로)
     */
    public String createImageWithTempTag(MultipartFile file, String dirName) {
        // validateFile(file); // 서비스 계층에서 이미 검증

        String s3Key = createFileName(file.getOriginalFilename(), dirName);

        try {
            // 1. PutObjectRequest로 파일 업로드
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(s3Key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            RequestBody requestBody = RequestBody.fromInputStream(file.getInputStream(), file.getSize());
            s3Client.putObject(putObjectRequest, requestBody);

            // 2. "Status: delete" 태그 추가
            Tag temporaryTag = Tag.builder().key("Status").value("delete").build();
            Tagging tagging = Tagging.builder().tagSet(Collections.singletonList(temporaryTag)).build();

            PutObjectTaggingRequest putTaggingRequest = PutObjectTaggingRequest.builder()
                    .bucket(bucket)
                    .key(s3Key)
                    .tagging(tagging)
                    .build();
            s3Client.putObjectTagging(putTaggingRequest);

            log.info("S3 파일 업로드 및 'Status: delete' 태그 추가 성공: {}", s3Key);
            return s3Key; // S3 Key (경로) 반환

        } catch (IOException e) {
            log.error("S3 파일 업로드 중 IO 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("파일 업로드 중 오류 발생", e);
        } catch (S3Exception e) {
            log.error("S3 서비스 오류 발생 ({}): {}", s3Key, e.awsErrorDetails().errorMessage(), e);
            throw new RuntimeException("S3 서비스 오류: " + e.awsErrorDetails().errorMessage(), e);
        }
    }

    /**
     * S3 객체에서 "Status: delete" 태그 제거
     * - 임시 파일이 아님을 표시하여 1일 후 삭제 규칙에서 제외시킵니다.
     * @param s3Key 태그를 제거할 객체의 S3 키
     * @return 성공 여부 (객체를 찾을 수 없는 경우 false)
     */
    public boolean deleteTempTag(String s3Key) {
        try {
            // 1. 현재 객체의 모든 태그 가져오기
            GetObjectTaggingRequest getTaggingRequest = GetObjectTaggingRequest.builder()
                    .bucket(bucket)
                    .key(s3Key)
                    .build();
            GetObjectTaggingResponse getTaggingResponse = s3Client.getObjectTagging(getTaggingRequest);
            List<Tag> currentTags = new ArrayList<>(getTaggingResponse.tagSet());

            // 2. "Status: delete" 태그만 필터링하여 제거
            List<Tag> updatedTags = currentTags.stream()
                    .filter(tag -> !(tag.key().equals("Status") && tag.value().equals("delete")))
                    .collect(Collectors.toList());

            // 3. 업데이트된 태그 세트로 객체 태그 다시 설정 (태그가 하나도 없으면 빈 리스트 전달)
            PutObjectTaggingRequest putTaggingRequest = PutObjectTaggingRequest.builder()
                    .bucket(bucket)
                    .key(s3Key)
                    .tagging(Tagging.builder().tagSet(updatedTags).build())
                    .build();

            s3Client.putObjectTagging(putTaggingRequest);

            log.info("S3 객체 {} 에서 'Status: delete' 태그 제거 성공.", s3Key);
            return true;
        } catch (NoSuchKeyException e) {
            log.warn("S3 객체 태그 제거 실패: 객체 {} 를 찾을 수 없습니다.", s3Key);
            return false;
        } catch (S3Exception e) {
            log.error("S3 객체 태그 제거 중 S3 오류 발생 ({}): {}", s3Key, e.awsErrorDetails().errorMessage(), e);
            return false;
        } catch (Exception e) {
            log.error("S3 객체 태그 제거 중 알 수 없는 오류 발생 ({}): {}", s3Key, e.getMessage(), e);
            return false;
        }
    }

    // --- 기존 유틸리티 메서드들 ---

    /**
     * S3에서 파일 삭제 (fileUrl에서 s3Key 추출)
     */
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            log.warn("삭제할 파일 URL이 null 또는 비어있습니다.");
            return;
        }
        String s3Key = getObjectKeyFromS3Url(fileUrl, bucket);
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(s3Key)
                    .build();
            s3Client.deleteObject(deleteObjectRequest);
            log.info("S3 파일 삭제 성공: {}", fileUrl);
        } catch (S3Exception e) {
            log.warn("S3 파일 삭제 실패 - S3 오류: {}", e.awsErrorDetails().errorMessage(), e);
        } catch (Exception e) {
            log.warn("S3 파일 삭제 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
        }
    }

}
