package com.github.jkky_98.noteJ.repository;

import com.github.jkky_98.noteJ.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c LEFT JOIN FETCH c.childrens WHERE c.post.id = :postId AND c.parent IS NULL")
    List<Comment> findByPostIdAndParentIsNull(@Param("postId") Long postId);
}
