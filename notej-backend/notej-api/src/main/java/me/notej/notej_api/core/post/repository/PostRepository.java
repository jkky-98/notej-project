package me.notej.notej_api.core.post.repository;

import me.notej.notej_api.core.post.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    @Query("SELECT m.memberUuid FROM Member m JOIN m.blog b JOIN Post p ON p.blog.id = b.id WHERE p.id = :postId")
    Optional<String> findMemberUuidByPostId(@Param("postId") Long postId);
}
