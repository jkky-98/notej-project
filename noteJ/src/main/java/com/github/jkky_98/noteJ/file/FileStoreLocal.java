package com.github.jkky_98.noteJ.file;

import com.github.jkky_98.noteJ.domain.FileMetadata;
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

    public String getFullPath(String fileName) {
        return fileDir + fileName;
    }

    public FileMetadata storeFile(MultipartFile file) throws IOException
    {
        if (file.isEmpty()) {
            return null;
        }

        FileMetadata buildFile = FileMetadata.builder()
                .originalFileName(file.getOriginalFilename())
                .storedFileName(createStoreFileName(file.getOriginalFilename()))
                .fileType(file.getContentType())
                .filePath(fileDir)
                .fileSize(file.getSize())
                .build();
        // 파일 저장
        file.transferTo(new File(getFullPath(buildFile.getStoredFileName())));

        // 파일이 저장된 후 경로가 제대로 반영되도록 확인 (캐시 관련 문제 해결)
        File savedFile = new File(getFullPath(buildFile.getStoredFileName()));
        if (savedFile.exists()) {
            // 서버에서 파일을 서빙할 수 있는지 체크
            System.out.println("File saved: " + savedFile.getAbsolutePath());
        }
        return buildFile;
    }

    private String createStoreFileName(String originalFilename) {
        String ext = extractExt(originalFilename);
        String uuid = UUID.randomUUID().toString();
        return uuid + "." + ext;
    }

    private String extractExt(String originalFilename) {
        int pos = originalFilename.lastIndexOf(".");
        return originalFilename.substring(pos + 1);
    }

}
