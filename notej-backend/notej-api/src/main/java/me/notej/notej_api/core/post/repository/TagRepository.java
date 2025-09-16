package me.notej.notej_api.core.post.repository;

import me.notej.notej_api.core.post.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {
}
