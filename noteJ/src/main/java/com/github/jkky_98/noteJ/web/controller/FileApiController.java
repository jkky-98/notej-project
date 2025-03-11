package com.github.jkky_98.noteJ.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@Profile({"local", "test"})
@Slf4j
public class FileApiController {

    // 파일 업로드 경로
    @Value("${file.dir}")
    private String uploadDir;

    /**
     * 에디터 이미지 업로드
     * @param image 파일 객체;
     * @return 업로드된 파일 명
     */
    @PostMapping("/editor/image-upload")
    public String uploadEditorImage(@RequestParam final MultipartFile image) {
        if (image.isEmpty()) {
            return "";
        }

        String orgFilename = image.getOriginalFilename();                                         // 원본 파일명
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");           // 32자리 랜덤 문자열
        String extension = orgFilename.substring(orgFilename.lastIndexOf(".") + 1);  // 확장자
        String saveFilename = uuid + "." + extension;                                             // 디스크에 저장할 파일명
        String fileFullPath = Paths.get(uploadDir, saveFilename).toString();

        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try {
            File uploadFile = new File(fileFullPath);
            image.transferTo(uploadFile);
            return saveFilename;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * 디스크에 업로드된 파일을 byte[]로 반환
     * @param filename 디스크에 업로드된 파일명
     * @return image byte array
     */
    @GetMapping(value = "/editor/editor-image-print", produces = {MediaType.IMAGE_GIF_VALUE, MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE})
    public byte[] printEditorImage(@RequestParam final String filename) {
        // 업로드 파일 전체 경로
        String fileFullPath = Paths.get(uploadDir, filename).toString();

        // 파일이 없는 경우 예외
        File uploadedFile = new File(fileFullPath);
        if (!uploadedFile.exists()) {
            throw new RuntimeException();
        }

        try {
            //이미지 파일을 바이트로 변환 후 반환
            return Files.readAllBytes(uploadedFile.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping(value = "/image-print/{fileUrl}", produces = {MediaType.IMAGE_GIF_VALUE, MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE})
    public ResponseEntity<byte[]> printImage(@PathVariable final String fileUrl) {

        // 이미지 파일 전체 경로
        String fileFullPath = Paths.get(uploadDir, fileUrl).toString();
        File uploadedFile = new File(fileFullPath);

        if (!uploadedFile.exists()) {
            log.error("File not found: {}", fileFullPath);
            return ResponseEntity.notFound().build();
        }

        try {
            // 파일 읽기
            byte[] imageBytes = Files.readAllBytes(uploadedFile.toPath());

            // Content-Type 설정
            String contentType = Files.probeContentType(uploadedFile.toPath());

            if (contentType == null) {
                log.warn("Unable to determine Content-Type for file: {}", fileUrl);
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .body(imageBytes);

        } catch (IOException e) {
            log.error("Error reading file: {}", fileFullPath, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping(value = "/image-print/default/{fileUrl}", produces = {MediaType.IMAGE_GIF_VALUE, MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE})
    public ResponseEntity<byte[]> printDefaultImage(@PathVariable final String fileUrl) {

        String fileUrlFinal = "default/" + fileUrl;

        // 이미지 파일 전체 경로
        String fileFullPath = Paths.get(uploadDir, fileUrlFinal).toString();
        File uploadedFile = new File(fileFullPath);
        if (!uploadedFile.exists()) {
            log.error("File not found: {}", fileFullPath);
            return ResponseEntity.notFound().build();
        }

        try {
            // 파일 읽기
            byte[] imageBytes = Files.readAllBytes(uploadedFile.toPath());

            // Content-Type 설정
            String contentType = Files.probeContentType(uploadedFile.toPath());

            if (contentType == null) {
                log.warn("Unable to determine Content-Type for file: {}", fileUrl);
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .body(imageBytes);

        } catch (IOException e) {
            log.error("Error reading file: {}", fileFullPath, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
