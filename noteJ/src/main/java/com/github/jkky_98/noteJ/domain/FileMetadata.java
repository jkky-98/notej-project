package com.github.jkky_98.noteJ.domain;

import com.github.jkky_98.noteJ.domain.user.UserDesc;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class FileMetadata {

    @Id
    @GeneratedValue
    @Column(name = "file_metadata_id")
    private Long id;

    private String originalFileName;  // 업로드된 파일 원본 이름
    private String storedFileName;    // 저장된 파일 이름
    private String filePath;          // 파일 경로 (예: /uploads/images/)
    private String fileType;          // 파일 타입 (예: image/png)
    private Long fileSize;            // 파일 크기

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "fileMetadata", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserDesc userDesc;  // 파일이 속하는 사용자

    public void updateFileMetadata(FileMetadata updateFileMetadata) {
        originalFileName = updateFileMetadata.getOriginalFileName();
        storedFileName = updateFileMetadata.getStoredFileName();
        filePath = updateFileMetadata.getFilePath();
        fileType = updateFileMetadata.getFileType();
        fileSize = updateFileMetadata.getFileSize();
    }
}

