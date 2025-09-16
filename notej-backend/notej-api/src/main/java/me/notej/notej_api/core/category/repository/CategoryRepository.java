package me.notej.notej_api.core.category.repository;

import me.notej.notej_api.core.blogmain.domain.Blog;
import me.notej.notej_api.core.category.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findAllByBlog_Url(String blogUrl);
    List<Category> findAllByBlog(Blog blog);
    // 특정 부모 카테고리 ID를 가진 카테고리들의 최대 seq를 찾는 쿼리
    // parentId가 null이면 최상위 카테고리들의 최대 seq를 찾음
    @Query("SELECT MAX(c.seq) FROM Category c WHERE c.blog.id = :blogId AND c.parent.id = :parentId")
    Integer findMaxSeqByParentId(@Param("blogId") Long blogId, @Param("parentId") Long parentId);

    // 최상위(parent가 null) 카테고리들의 최대 seq를 찾는 쿼리
    @Query("SELECT MAX(c.seq) FROM Category c WHERE c.blog.id = :blogId AND c.parent IS NULL")
    Integer findMaxSeqForRootCategories(@Param("blogId") Long blogId);
}
