package com.github.jkky_98.noteJ.repository;

import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.web.controller.dto.TagCountDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String loginId);
    Optional<User> findByEmail(String email);
//
    @Query("SELECT new com.github.jkky_98.noteJ.web.controller.dto.TagCountDto(t.name, COUNT(t)) " +
            "FROM User u " +
            "JOIN u.posts p " +
            "JOIN p.postTags pt " +
            "JOIN pt.tag t " +
            "WHERE u.id = :userId " +
            "AND p.writable = true " +
            "GROUP BY t.name")
    List<TagCountDto> findTagsByUser(@Param("userId") Long userId);

}
