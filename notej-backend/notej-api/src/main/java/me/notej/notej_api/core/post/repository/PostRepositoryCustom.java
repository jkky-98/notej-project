package me.notej.notej_api.core.post.repository;

import me.notej.notej_api.core.post.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostRepositoryCustom {
    Page<Post> findFilteredPosts(
            String blogUrl,
            String categoryName,
            String search, // 검색어 (제목, 내용)
            String tagName, // 특정 태그 필터링
            String sortBy,  // 정렬 기준
            Pageable pageable
    );
}
