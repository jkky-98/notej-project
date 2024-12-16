package com.github.jkky_98.noteJ.repository;

import com.github.jkky_98.noteJ.domain.Post;
import com.github.jkky_98.noteJ.repository.post.PostRepositoryCustom;
import com.github.jkky_98.noteJ.web.controller.dto.TagCountDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {

    @Query("SELECT p " +
            "FROM User u " +
            "JOIN u.posts p " + // 연관 필드를 기준으로 조인
            "WHERE p.postUrl = :postUrl AND u.username = :username")
    Post findOnePost(@Param("username") String username, @Param("postUrl") String postUrl);

}
