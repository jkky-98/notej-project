package me.notej.notej_api.core.post.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.notej.notej_api.aws.s3.S3Bucket;
import me.notej.notej_api.core.member.domain.Member;
import me.notej.notej_api.core.member.repository.MemberRepository;
import me.notej.notej_api.core.post.dto.EditorImageUploadResponse;
import me.notej.notej_api.core.post.dto.PostThumbnailImageUploadResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostImageService {

    private final S3Bucket s3Bucket;
    private final MemberRepository memberRepository;

    // 모든 이미지 업로드에 공통되는 핵심 로직 (member 찾기, S3 업로드)
    @Transactional
    public EditorImageUploadResponse uploadImage(Authentication authentication, MultipartFile imageFile) {
        String s3Key = performS3Upload(authentication, imageFile, "posts", true); // 'posts' 기본 경로
        return new EditorImageUploadResponse(s3Key);
    }

    @Transactional
    public PostThumbnailImageUploadResponse uploadThumbnail(Authentication authentication, MultipartFile imageFile) {
        String s3Key = performS3Upload(authentication, imageFile, "posts/thumbnail", false); // 'posts/thumbnail' 경로
        return new PostThumbnailImageUploadResponse(s3Key);
    }

    public void deleteImage(String s3Key) {
        s3Bucket.deleteFile(s3Key);
    }

    private String performS3Upload(Authentication authentication, MultipartFile imageFile, String dirPath, boolean isUseTempTag) {
        validateImageFile(imageFile); // 먼저 파일 검증

        User user = (User) authentication.getPrincipal();
        String memberUuid = user.getUsername();

        Member member = memberRepository.findByMemberUuid(memberUuid)
                .orElseThrow(() -> new EntityNotFoundException("USER_NOT_FOUND"));
        Long memberId = member.getId();

        String finalDirPath = dirPath + "/" + memberId; // memberId를 경로에 추가

        try {
            String s3Key = null;
            if (isUseTempTag) {
                s3Key = s3Bucket.createImageWithTempTag(imageFile, finalDirPath);
            } else {
                s3Key = s3Bucket.createImage(imageFile, finalDirPath);
            }
            log.info("[PostImageService][performS3Upload] 이미지 S3 업로드 성공 (경로: {}) : {}", finalDirPath, s3Key);
            return s3Key;
        } catch (RuntimeException e) {
            log.error("이미지 S3 업로드 중 RuntimeException 발생 (경로: {}) : {}", finalDirPath, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("예상치 못한 이미지 S3 업로드 오류 (경로: {}) : {}", finalDirPath, e.getMessage(), e);
            throw new RuntimeException("이미지 업로드 중 알 수 없는 오류가 발생했습니다.", e);
        }
    }

    public byte[] downloadImage(String filename) {
        log.info("[EditorImageService][downloadImage] 사진 비동기 다운로드 성공 : {}", filename);
        return s3Bucket.getBytesFromObject(filename);
    }

    private void validateImageFile(MultipartFile imageFile) {
        // 1. 빈 파일 검증
        if (imageFile.isEmpty()) {
            throw new IllegalArgumentException("빈 파일 입니다.");
        }
        // 2. 파일 타입 검증
        String contentType = imageFile.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
        }
        // 3. 파일 크기 검증
        long maxSize = 10 * 1024 * 1024; // 10MB

        if (imageFile.getSize() > maxSize) {
            throw new IllegalArgumentException("파일 크기는 10MB 이하여야 합니다.");
        }
    }

}
