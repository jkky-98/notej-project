package com.github.jkky_98.noteJ.repository;

import com.github.jkky_98.noteJ.domain.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {
}
