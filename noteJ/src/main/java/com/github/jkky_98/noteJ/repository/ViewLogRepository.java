package com.github.jkky_98.noteJ.repository;

import com.github.jkky_98.noteJ.domain.ViewLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ViewLogRepository extends JpaRepository<ViewLog, Long> {

    @Query("SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END " +
            "FROM ViewLog v " +
            "WHERE v.post.id = :postId " +
            "AND v.identifier = :identifier " +
            "AND v.createDt > :timeLimit")
    boolean existsRecentView(@Param("postId") Long postId,
                             @Param("identifier") String identifier,
                             @Param("timeLimit") LocalDateTime timeLimit);

    /**
     * 최근 1시간 동안 동일 사용자의 조회 횟수 카운트 (악성 패턴 감지)
     */
    @Query("SELECT COUNT(v) FROM ViewLog v WHERE v.identifier = :identifier AND v.createDt > :timeLimit")
    long countRecentViews(@Param("identifier") String identifier,
                          @Param("timeLimit") LocalDateTime timeLimit);

}
