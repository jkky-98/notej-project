package com.github.jkky_98.noteJ.file;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Component
@Profile("local")
public class FileStoreLocal implements FileStore{

    @Value("${file.dir}")
    private String fileDir;

    @Override
    public String getFullPath(String fileName) {
        return fileDir + fileName;
    }

    @Override
    public String storeFile(MultipartFile file) throws IOException
    {
        if (file.isEmpty()) {
            return null;
        }

        String storeFileName = createStoredFileName(file.getOriginalFilename());
        // 파일 저장
        file.transferTo(new File(getFullPath(storeFileName)));

        // 파일이 저장된 후 경로가 제대로 반영되도록 확인 (캐시 관련 문제 해결)
        File savedFile = new File(getFullPath(storeFileName));
        if (savedFile.exists()) {
            // 서버에서 파일을 서빙할 수 있는지 체크
            System.out.println("File saved: " + savedFile.getAbsolutePath());
        }

        return storeFileName;
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

}
