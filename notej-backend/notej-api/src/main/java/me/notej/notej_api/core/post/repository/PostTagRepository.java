package me.notej.notej_api.core.post.repository;

import me.notej.notej_api.core.post.domain.PostTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostTagRepository extends JpaRepository<PostTag, Long> {
}
