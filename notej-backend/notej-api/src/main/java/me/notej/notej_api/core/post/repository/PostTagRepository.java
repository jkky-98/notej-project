package me.notej.notej_api.core.post.repository;

import me.notej.notej_api.core.post.domain.PostTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PostTagRepository extends JpaRepository<PostTag, Long> {
    long countByTagId(Long tagId);
}
