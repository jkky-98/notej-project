package me.notej.notej_api.core.post.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.notej.notej_api.aws.s3.S3Bucket;
import me.notej.notej_api.core.member.domain.Member;
import me.notej.notej_api.core.member.repository.MemberRepository;
import me.notej.notej_api.core.post.dto.EditorImageUploadResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class EditorImageService {

    private final S3Bucket s3Bucket;
    private final MemberRepository memberRepository;

    @Transactional
    public EditorImageUploadResponse uploadImage(Authentication authentication, MultipartFile imageFile) {
        // 이미지 파일 검증 로직
        validateImageFile(imageFile);

        // 유저 뽑기
        User user = (User) authentication.getPrincipal();
        String memberUuid = user.getUsername();

        Member member = memberRepository.findByMemberUuid(memberUuid).orElseThrow(() -> new EntityNotFoundException("USER_NOT_FOUND"));
        Long memberId = member.getId();

        // S3 파일 저장 로직, 임시 태그 필요
        try {
            // 사진 객체 저장시 memberId 별로 경로 분리를 위해 memberId를 최종경로에 추가
            String dirName = "posts/" + memberId;

            // S3에 업로드 + Status: delete 태그 추가
            String s3Key = s3Bucket.uploadWithTemporaryDeletionTag(imageFile, dirName);

            log.info("[EditorImageService][uploadImage] 에디터 사진 비동기 업로드 성공 : {}", s3Key );
            return new EditorImageUploadResponse(s3Key);
        } catch (RuntimeException e) {
            log.error("이미지 업로드 서비스 실패: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("예상치 못한 이미지 업로드 서비스 오류: {}", e.getMessage(), e);
            throw new RuntimeException("이미지 업로드 중 알 수 없는 오류가 발생했습니다.", e);
        }
    }

    public byte[] downloadImage(String filename) {
        log.info("[EditorImageService][downloadImage] 에디터 사진 비동기 다운로드 성공 : {}", filename);
        return s3Bucket.getImageBytes(filename);
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
