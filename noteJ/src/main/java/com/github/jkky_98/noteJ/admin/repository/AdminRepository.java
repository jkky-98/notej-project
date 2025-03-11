package com.github.jkky_98.noteJ.admin.repository;

import com.github.jkky_98.noteJ.admin.dto.AdminContentsCond;
import com.github.jkky_98.noteJ.admin.dto.AdminUsersCond;
import com.github.jkky_98.noteJ.domain.*;
import com.github.jkky_98.noteJ.domain.user.QUser;
import com.github.jkky_98.noteJ.domain.user.QUserDesc;
import com.github.jkky_98.noteJ.domain.user.User;
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

    public Page<User> searchUsersAdmin(AdminUsersCond cond, Pageable pageable) {
        QUser u = QUser.user;

        List<User> users = queryFactory
                .select(u)
                .from(u)
                .where(
                        usernameSearchCondition(cond.getUsername(), u),
                        emailSearchCondition(cond.getEmail(), u),
                        blogTitleSearchCondition(cond.getBlogTitle(), u.userDesc)
                )
                .orderBy(u.createDt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = Optional.ofNullable(queryFactory
                .select(u.count())
                .from(u)
                .where(
                        usernameSearchCondition(cond.getUsername(), u),
                        emailSearchCondition(cond.getEmail(), u),
                        blogTitleSearchCondition(cond.getBlogTitle(), u.userDesc)
                )
                .fetchOne()).orElse(0L);

        return new PageImpl<>(users, pageable, total);
    }

    private BooleanExpression usernameSearchCondition(String username, QUser u) {
        return (username != null && !username.isEmpty()) ? u.username.containsIgnoreCase(username) : null;
    }

    private BooleanExpression emailSearchCondition(String userEmail, QUser u) {
        return (userEmail != null && !userEmail.isEmpty()) ? u.email.containsIgnoreCase(userEmail) : null;
    }

    // blogTitle in userDesc
    private BooleanExpression blogTitleSearchCondition(String blogTitle, QUserDesc uDesc) {
        return (blogTitle != null && !blogTitle.isEmpty()) ? uDesc.blogTitle.containsIgnoreCase(blogTitle) : null;
    }
}

