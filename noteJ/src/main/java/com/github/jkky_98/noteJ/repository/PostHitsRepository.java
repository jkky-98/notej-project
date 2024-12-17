package com.github.jkky_98.noteJ.repository;

import com.github.jkky_98.noteJ.domain.Post;
import com.github.jkky_98.noteJ.domain.PostHits;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostHitsRepository extends JpaRepository<PostHits, Long> {

    @Query("SELECT ph " +
            "FROM PostHits ph " +
            "INNER JOIN ph.post p " +
            "WHERE p.postUrl = :postUrl")
    List<PostHits> findAllPostHitsByPostId(@Param("postUrl") String postUrl);
}
