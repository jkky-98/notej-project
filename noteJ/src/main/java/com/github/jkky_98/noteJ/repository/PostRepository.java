package com.github.jkky_98.noteJ.repository;

import com.github.jkky_98.noteJ.domain.Post;
import com.github.jkky_98.noteJ.repository.post.PostRepositoryCustom;
import com.github.jkky_98.noteJ.web.controller.dto.TagCountDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {

    Optional<Post> findByPostUrl(String postUrl);

    List<Post> findAllByUserIdAndWritableFalse(Long userId);

    @Query("SELECT p from Post p WHERE p.writable = true AND p.title LIKE %:keyword%")
    Page<Post> findAllByWritableTrue(String keyword, Pageable pageable);
}
