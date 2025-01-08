package com.github.jkky_98.noteJ.file;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileStore {

    /**
     * 파일을 저장하고, 해당 파일에 대한 메타데이터를 반환합니다.
     * @param file 업로드할 파일
     * @return 저장된 파일의 메타데이터
     * @throws IOException 예외 처리
     */
    String storeFile(MultipartFile file) throws IOException;

    String getFullPath(String fileName);

    String deleteFile(String thumbnailDeleted);
}
