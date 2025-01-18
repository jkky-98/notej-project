package com.github.jkky_98.noteJ.repository;

import com.github.jkky_98.noteJ.domain.PostFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostFileRepository extends JpaRepository<PostFile, Long> {
}
