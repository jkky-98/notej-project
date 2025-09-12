package me.notej.notej_api.core.blogmain.repository;

import me.notej.notej_api.core.blogmain.domain.Blog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BlogRepository extends JpaRepository<Blog, Long> {
    boolean existsByUrl(String blogUrl);
    Optional<Blog> findByUrl(String blogUrl);
}
