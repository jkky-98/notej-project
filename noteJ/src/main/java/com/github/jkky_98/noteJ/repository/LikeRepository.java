package com.github.jkky_98.noteJ.repository;

import com.github.jkky_98.noteJ.domain.Like;
import com.github.jkky_98.noteJ.domain.Post;
import com.github.jkky_98.noteJ.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {

    boolean existsByUserAndPostPostUrl(User user, String postUrl);

    boolean existsByUserAndPost(User user, Post post);

    Optional<Like> findByUserAndPost(User user, Post post);

    @Query("select count(l) from Like l where l.userGetLike.id = :userId")
    long countLikesByUserId(@Param("userId") Long userId);

    @Query("select count(l) from Like l where l.userGetLike.id = :userId and l.post.series.seriesName = :seriesName")
    long countLikesByUserIdAndSeriesName(@Param("userId") Long userId, @Param("seriesName") String seriesName);
}
