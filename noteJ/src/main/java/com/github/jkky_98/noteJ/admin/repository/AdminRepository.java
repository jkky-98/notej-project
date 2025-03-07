package com.github.jkky_98.noteJ.admin.repository;

import com.github.jkky_98.noteJ.admin.dto.AdminContentsCond;
import com.github.jkky_98.noteJ.domain.*;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AdminRepository {

    private final JPAQueryFactory queryFactory;

    @Autowired
    public AdminRepository(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    public Page<Contact> searchContactsAdmin(Pageable pageable) {
        QContact c = QContact.contact;

        List<Contact> contacts = queryFactory
                .select(c)
                .from(c)
                .orderBy(c.createDt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = Optional.ofNullable(
                queryFactory
                .select(c.count())
                .from(c)
                .fetchOne()).orElse(0L);

        return new PageImpl<>(contacts, pageable, total);
    }

    public Page<Post> searchPostsAdmin(AdminContentsCond cond, Pageable pageable) {
        QPost p = QPost.post; // QPost 객체 사용

        // 검색 조건 적용
        List<Post> posts = queryFactory
                .select(p)
                .from(p)
                .where(
                        p.writable.eq(true),
                        postSearchCondition(cond, p)
                )
                .orderBy(p.createDt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 개수 조회 (null 방지)
        Long total = Optional.ofNullable(queryFactory
                .select(p.count())
                .from(p)
                .where(
                        p.writable.eq(true),
                        postSearchCondition(cond, p)
                )
                .fetchOne()).orElse(0L);

        return new PageImpl<>(posts, pageable, total);
    }

    private BooleanExpression postSearchCondition(AdminContentsCond cond, QPost p) {
        String search = cond.getSearch();
        return (search != null && !search.isEmpty()) ? p.title.containsIgnoreCase(search) : null;
    }
}

