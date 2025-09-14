package me.notej.notej_api.aws.s3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileImageService {

    private final S3Bucket s3Bucket;

    // 프로필 이미지 URL 생성 메소드
    public String getProfileImageUrl(String objectKey) {
        if (objectKey == null) {
            return null;
        }

        return s3Bucket.generateSignedUrl(objectKey, Duration.ofMinutes(30));
    }
}