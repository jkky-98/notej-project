package com.github.jkky_98.noteJ.repository.post;

import com.github.jkky_98.noteJ.domain.*;
import com.github.jkky_98.noteJ.domain.user.QUser;
import com.github.jkky_98.noteJ.web.controller.form.PostsConditionForm;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;

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
                        searchCondition(form.getSearch(), p),
                        tagCondition(form.getTagName(), p, pt, t),
                        seriesCondition(form.getSeriesName(), p, s),
                        userCondition(username, u)
                )
                .orderBy(p.createDt.desc()) // 최신 날짜 순으로 정렬
                .distinct()
                .fetch();
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
