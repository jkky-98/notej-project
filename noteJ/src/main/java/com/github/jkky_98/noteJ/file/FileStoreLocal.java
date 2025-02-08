package com.github.jkky_98.noteJ.file;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
@Profile("local")
public class FileStoreLocal implements FileStore{

    @Value("${file.dir}")
    private String fileDir;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "webp");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB 제한
    private static int profilePicSize = 120;

    @Override
    public String getFullPath(String fileName) {
        return fileDir + fileName;
    }

    @Override
    public String storeFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("파일이 비어 있습니다.");
        }

        // 파일 크기 제한 체크
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IOException("파일 크기가 너무 큽니다. (최대 10MB)");
        }

        // 파일 확장자 체크
        String originalFilename = file.getOriginalFilename();
        if (!isAllowedExtension(originalFilename)) {
            throw new IOException("허용되지 않은 파일 형식입니다.");
        }

        String extension = getFileExtension(originalFilename); // 파일 확장자 추출

        if (extension.isEmpty()) {
            throw new IOException("파일 확장자를 확인할 수 없습니다.");
        }

        // PNG → JPEG 변환 여부 확인
        boolean isPngToJpeg = extension.equalsIgnoreCase("png");

        // 저장할 파일명 설정 (PNG → JPEG 변환 시 확장자 변경)
        String storeFileName = createStoredFileName(originalFilename);
        if (isPngToJpeg) {
            storeFileName = storeFileName.replace(".png", ".jpeg"); // 확장자 변경
        }

        File destination = new File(getFullPath(storeFileName));

        // 이미지 읽기
        BufferedImage image = ImageIO.read(file.getInputStream());

        // PNG → JPEG 변환 시 투명도 제거
        if (isPngToJpeg) {
            image = convertPngToJpeg(image);
        }

        // 이미지 크기 조정 및 저장
        try {
            if (image.getWidth() > profilePicSize || image.getHeight() > profilePicSize) {
                Thumbnails.of(image)
                        .size(profilePicSize, profilePicSize)
                        .outputFormat("jpeg") // JPEG로 변환
                        .outputQuality(0.8f)
                        .toFile(destination);
            } else {
                Thumbnails.of(image)
                        .size(image.getWidth(), image.getHeight())
                        .outputFormat("jpeg") // JPEG로 변환
                        .outputQuality(0.8f)
                        .toFile(destination);
            }
        } catch (Exception e) {
            throw e;
        }

        return storeFileName;
    }

    /**
     * PNG 이미지를 JPEG로 변환하면서 투명도(Alpha 채널) 제거 -> 흰색으로
     */
    private BufferedImage convertPngToJpeg(BufferedImage image) {
        BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = newImage.createGraphics();

        // 배경을 흰색으로 설정
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, image.getWidth(), image.getHeight());

        // 원본 이미지 그리기 (투명도 제거)
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();

        return newImage;
    }




    // 파일 확장자 추출 메서드 추가
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }


    @Override
    public String deleteFile(String thumbnailDeleted) {
        // 파일 경로 생성
        File fileToDelete = new File(getFullPath(thumbnailDeleted));

        // 파일이 존재하면 삭제 시도
        if (fileToDelete.exists()) {
            if (fileToDelete.delete()) {
                return thumbnailDeleted;
            } else {
                throw new RuntimeException("Failed to delete file: " + fileToDelete.getAbsolutePath());
            }
        } else {
            throw new RuntimeException("File not found: " + fileToDelete.getAbsolutePath());
        }
    }

    private String createStoredFileName(String originalFilename) {
        String ext = extractExt(originalFilename);
        String uuid = UUID.randomUUID().toString();
        return uuid + "." + ext;
    }

    private String extractExt(String originalFilename) {
        int pos = originalFilename.lastIndexOf(".");
        return originalFilename.substring(pos + 1);
    }

    private boolean isAllowedExtension(String filename) {
        String ext = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        return ALLOWED_EXTENSIONS.contains(ext);
    }
}
