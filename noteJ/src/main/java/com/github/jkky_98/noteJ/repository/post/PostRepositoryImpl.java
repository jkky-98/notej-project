package com.github.jkky_98.noteJ.repository.post;

import com.github.jkky_98.noteJ.domain.*;
import com.github.jkky_98.noteJ.domain.user.QUser;
import com.github.jkky_98.noteJ.web.controller.form.PostsConditionForm;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

public class PostRepositoryImpl implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public PostRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<Post> searchPosts(PostsConditionForm form, String username) {

        QPost p = new QPost("p");
        QPostTag pt =  new QPostTag("pt");
        QTag t = new QTag("t");
        QSeries s = new QSeries("s");
        QUser u = new QUser("u");

        return queryFactory
                .select(p)
                .from(p)
                .innerJoin(p.user, u).fetchJoin()
                .leftJoin(p.postTags, pt).fetchJoin()
                .leftJoin(pt.tag, t).fetchJoin()
                .leftJoin(p.series, s).fetchJoin()
                .where(
                        p.writable.eq(true),
                        searchCondition(form.getSearch(), p),
                        tagCondition(form.getTagName(), p, pt, t),
                        seriesCondition(form.getSeriesName(), p, s),
                        userCondition(username, u)
                )
                .orderBy(p.createDt.desc()) // 최신 날짜 순으로 정렬
                .limit(5)
                .distinct()
                .fetch();
    }

    @Override
    public Page<Post> searchPostsWithPage(PostsConditionForm form, String username, Pageable pageable) {
        QPost p = new QPost("p");
        QPostTag pt = new QPostTag("pt");
        QTag t = new QTag("t");
        QSeries s = new QSeries("s");
        QUser u = new QUser("u");

        // 기본 쿼리
        JPQLQuery<Post> query = queryFactory
                .select(p)
                .from(p)
                .innerJoin(p.user, u).fetchJoin()
                .leftJoin(p.postTags, pt).fetchJoin()
                .leftJoin(pt.tag, t).fetchJoin()
                .leftJoin(p.series, s).fetchJoin()
                .where(
                        p.writable.eq(true),
                        searchCondition(form.getSearch(), p),
                        tagCondition(form.getTagName(), p, pt, t),
                        seriesCondition(form.getSeriesName(), p, s),
                        userCondition(username, u)
                )
                .distinct();

        // 페이징 설정 (offset과 limit)
        long total = query.fetchCount(); // 전체 개수 조회
        List<Post> content = query
                .orderBy(p.createDt.desc()) // 정렬
                .offset(pageable.getOffset()) // 시작 인덱스
                .limit(pageable.getPageSize()) // 개수 제한
                .fetch();

        // Page 객체 반환
        return new PageImpl<>(content, pageable, total);
    }

    private BooleanExpression userCondition(String username, QUser u) {
        return username != null && !username.isEmpty()
                ? u.username.eq(username)
                : null;
    }

    private BooleanExpression searchCondition(String search, QPost p) {
        return search != null && !search.isEmpty()
                ? p.title.containsIgnoreCase(search)
                : null;
    }

    private BooleanExpression seriesCondition(String seriesName, QPost p, QSeries s) {
        return seriesName != null && !seriesName.isEmpty()
                ? p.series.seriesName.eq(seriesName)
                : null;
    }

    private BooleanExpression tagCondition(String tagName, QPost p, QPostTag pt, QTag t) {
        return tagName != null && !tagName.isEmpty()
                ? JPAExpressions.selectOne()
                .from(pt)
                .join(pt.tag, t)
                .where(pt.post.eq(p).and(t.name.eq(tagName)))
                .exists()
                : null;
    }
}
