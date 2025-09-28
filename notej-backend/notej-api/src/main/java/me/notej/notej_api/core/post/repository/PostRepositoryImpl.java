package me.notej.notej_api.core.post.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.notej.notej_api.core.blogmain.domain.QBlog;
import me.notej.notej_api.core.category.domain.QCategory;
import me.notej.notej_api.core.post.domain.Post;
import me.notej.notej_api.core.post.domain.QPost;
import me.notej.notej_api.core.post.domain.QPostTag;
import me.notej.notej_api.core.post.domain.QTag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class PostRepositoryImpl implements PostRepositoryCustom{

    QPost p = new QPost("p");
    QPostTag pt = new QPostTag("pt");
    QTag t = new QTag("t");
    QBlog b = new QBlog("b");
    QCategory c = new QCategory("c");

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Post> findFilteredPosts(
            String blogUrl,
            String categoryName,
            String search,
            String tagName,
            String sortBy,
            Pageable pageable) {
        // 기본 쿼리 구성
        JPAQuery<Post> query = queryFactory
                .selectFrom(p)
                .innerJoin(p.blog, b).fetchJoin()
                .innerJoin(p.category, c).fetchJoin()
                .leftJoin(p.postTags, pt)
                .innerJoin(pt.tag, t)
                .where(
                        b.url.eq(blogUrl),
                        p.active.eq(true),
                        categoryNameEq(categoryName),
                        searchEq(search),
                        tagNameEq(tagName)
                )
                .distinct()
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        // 정렬 조건
        OrderSpecifier<?> orderSpecifier = getOrderSpecifier(sortBy);
        if (orderSpecifier != null) {
            query.orderBy(orderSpecifier);
        }
        // 쿼리 결과
        List<Post> content = query.fetch();

        // 총 개수 쿼리
        Long total = queryFactory
                .select(p.id.countDistinct()) // p.id를 기준으로 중복 없는 개수를 센다.
                .from(p)
                .innerJoin(p.blog, b)
                .innerJoin(p.category, c)
                .leftJoin(p.postTags, pt)
                .innerJoin(pt.tag, t)
                .where(
                        b.url.eq(blogUrl),
                        p.active.eq(true),
                        categoryNameEq(categoryName),
                        searchEq(search),
                        tagNameEq(tagName)
                )
                .fetchOne(); // 단일 결과 반환

        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }

    // 카테고리 이름 검색 조건
    private BooleanExpression categoryNameEq(String categoryName) {
        return categoryName != null
                ? p.category.name.eq(categoryName)
                : null;
    }

    // 검색어 검색 조건 ( 제목 타깃으로 검색 )
    private BooleanExpression searchEq(String search) {
        log.info(search);
        return search != null && !search.isEmpty()
                ? p.title.containsIgnoreCase(search)
                : null;
    }

    // 태그 이름 검색 조건
    private BooleanExpression tagNameEq(String tagName) {
        return tagName != null && !tagName.isEmpty()
                ? t.name.eq(tagName)
                : null;
    }

    // 정렬 조건에 따른 OrderSpecifier
    private OrderSpecifier<?> getOrderSpecifier(String sortBy) {
        if (!StringUtils.hasText(sortBy)) {
            return p.updated_at.desc();
        }

        return switch (sortBy) {
            case "latest" -> p.updated_at.desc();
            case "oldest" -> p.updated_at.asc();
//            case "popular" -> p.views.desc();
            default -> p.updated_at.desc();
        };
    }

}
