package com.github.jkky_98.noteJ.web.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectTagging;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/editor")
@Profile("!local")
@RequiredArgsConstructor
public class FileApiProdController {

    // 파일 업로드 경로
    @Value("${cloud.aws.s3.bucket}")
    private String s3BucketName;

    private final AmazonS3 amazonS3;

    /**
     * 에디터 이미지 업로드
     * @param image 파일 객체;
     * @return 업로드된 파일 명
     */
    @PostMapping("/image-upload")
    public String uploadEditorImage(@RequestParam final MultipartFile image) {
        if (image.isEmpty()) {
            return "";
        }

        String orgFilename = image.getOriginalFilename();
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        String extension = orgFilename.substring(orgFilename.lastIndexOf(".") + 1);
        String saveFilename = uuid + "." + extension;

        try (InputStream inputStream = image.getInputStream()) {
            // S3에 파일 업로드
            // 태그 추가
            List<Tag> tags = new ArrayList<>();
            tags.add(new Tag("Status", "delete")); // "Status=delete" 태그 추가

            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    s3BucketName,
                    saveFilename,
                    inputStream,
                    null).withTagging(new ObjectTagging(tags));

            amazonS3.putObject(putObjectRequest);

            return saveFilename;
        } catch (IOException e) {
            throw new RuntimeException("Error uploading file to S3", e);
        }
    }
    /**
     * 디스크에 업로드된 파일을 byte[]로 반환
     * @param filename 디스크에 업로드된 파일명
     * @return image byte array
     */
    @GetMapping(value = "/image-print", produces = {MediaType.IMAGE_GIF_VALUE, MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE})
    public byte[] printEditorImage(@RequestParam final String filename) {
        // S3 객체 가져오기
        S3Object s3Object = amazonS3.getObject(s3BucketName, filename);
        try {
            return s3Object.getObjectContent().readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("Error download and print from S3");
        }
    }

}
