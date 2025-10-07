package me.notej.notej_api.core.post.repository;

import me.notej.notej_api.core.post.domain.Tag;
import me.notej.notej_api.core.post.dto.TagResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    @Query("SELECT new me.notej.notej_api.core.post.dto.TagResponse(t.name, COUNT(t.id)) " +
            "FROM Tag t " +
            "JOIN PostTag pt ON pt.tag.id = t.id " +
            "JOIN Post p ON pt.post.id = p.id " +
            "JOIN Blog b ON p.blog.id = b.id " +
            "WHERE b.url = :blogUrl " +
            "GROUP BY t.id " +
            "ORDER BY COUNT(t.id) DESC"
    )
    List<TagResponse> findTagResponseByBlogUrl(@Param("blogUrl") String blogUrl);
    Optional<Tag> findTagByName(String name);
    Boolean existsByName(String name);
}
