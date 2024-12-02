package com.github.jkky_98.noteJ.repository;

import com.github.jkky_98.noteJ.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
