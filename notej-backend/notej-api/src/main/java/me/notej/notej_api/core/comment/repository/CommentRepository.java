package me.notej.notej_api.core.comment.repository;

import me.notej.notej_api.core.comment.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    // 특정 게시글의 모든 댓글 조회 (시간순 정렬)
    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId ORDER BY c.created_at ASC")
    List<Comment> findAllByPostIdOrderByCreatedAtAsc(@Param("postId") Long postId);

    // 특정 게시글의 최상위 댓글만 조회 (시간순 정렬)
    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId AND c.parent IS NULL ORDER BY c.created_at ASC")
    List<Comment> findTopLevelCommentsByPostIdOrderByCreatedAtAsc(@Param("postId") Long postId);

    // 특정 댓글의 모든 답글 조회 (시간순 정렬)
    @Query("SELECT c FROM Comment c WHERE c.parent.id = :parentId ORDER BY c.created_at ASC")
    List<Comment> findRepliesByParentIdOrderByCreatedAtAsc(@Param("parentId") Long parentId);

    // 특정 게시글의 댓글 수 카운트
    long countByPostId(Long postId);

    // 특정 사용자가 작성한 댓글 목록 조회
    @Query("SELECT c FROM Comment c WHERE c.member.id = :memberId ORDER BY c.created_at DESC")
    List<Comment> findByMemberIdOrderByCreatedAtDesc(Long authorId);
}
