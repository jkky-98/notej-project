package me.notej.notej_api.core.post.repository;

import me.notej.notej_api.core.post.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
