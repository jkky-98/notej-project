package com.github.jkky_98.noteJ.file;

import com.github.jkky_98.noteJ.domain.FileMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Component
public class FileStore {

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

        file.transferTo(new File(getFullPath(buildFile.getStoredFileName())));
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
