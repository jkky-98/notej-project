package com.github.jkky_98.noteJ.web.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectTagging;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@Profile({"green", "blue"})
@RequiredArgsConstructor
@Slf4j
public class FileApiProdController {

    // 파일 업로드 경로
    @Value("${cloud.aws.s3.bucket}")
    private String s3BucketName;

    private final AmazonS3 amazonS3;


    @PostMapping("/editor/image-upload")
    public String uploadEditorImage(@RequestParam final MultipartFile image) {
        if (image.isEmpty()) {
            return "";
        }

        String saveFilename = generateSaveFilename(image);

        try (InputStream inputStream = image.getInputStream()) {

            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    s3BucketName,
                    saveFilename,
                    inputStream,
                    null)
                    .withTagging(generateDeleteTag());
                    // 에디터에서 사진 업로드시 delete 태그를 s3객체에 부착해야 한다.

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
    @GetMapping(value = "/editor/image-print", produces = {MediaType.IMAGE_GIF_VALUE, MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE})
    public byte[] printEditorImage(@RequestParam final String filename) {
        S3Object s3Object = getS3Object(filename);
        try {
            return s3Object.getObjectContent().readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("Error download and print from S3");
        }
    }

    @GetMapping(value = "/image-print/{fileUrl}", produces = {MediaType.IMAGE_GIF_VALUE, MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE})
    public ResponseEntity<byte[]> printImage(@PathVariable final String fileUrl) {
        S3Object s3Object = getS3Object(fileUrl);

        try (InputStream inputStream = s3Object.getObjectContent()) {
            byte[] imageBytes = inputStream.readAllBytes();

            // Determine Content-Type based on file extension
            String contentType = Files.probeContentType(Paths.get(fileUrl));

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(imageBytes);

        } catch (IOException e) {
            throw new RuntimeException("Error downloading and printing from S3", e);
        }
    }

    @GetMapping(value = "/image-print/default/{fileUrl}", produces = {MediaType.IMAGE_GIF_VALUE, MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE})
    public ResponseEntity<byte[]> printDefaultImage(@PathVariable final String fileUrl) {
        String fileUrlFinal = "default/" + fileUrl;
        S3Object s3Object = getS3Object(fileUrlFinal);

        try (InputStream inputStream = s3Object.getObjectContent()) {
            byte[] imageBytes = inputStream.readAllBytes();

            // Determine Content-Type based on file extension
            String contentType = Files.probeContentType(Paths.get(fileUrl));

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(imageBytes);

        } catch (IOException e) {
            throw new RuntimeException("Error downloading and printing from S3", e);
        }
    }

    private S3Object getS3Object(String filename) {
        return amazonS3.getObject(s3BucketName, filename);
    }

    private static ObjectTagging generateDeleteTag() {
        List<Tag> tags = new ArrayList<>();
        tags.add(new Tag("Status", "delete"));
        return new ObjectTagging(tags);
    }

    private static String generateSaveFilename(MultipartFile image) {
        String orgFilename = image.getOriginalFilename();
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        String extension = orgFilename.substring(orgFilename.lastIndexOf(".") + 1);
        String saveFilename = uuid + "." + extension;
        return saveFilename;
    }
}
