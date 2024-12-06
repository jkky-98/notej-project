package com.github.jkky_98.noteJ.repository;

import com.github.jkky_98.noteJ.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {
}
