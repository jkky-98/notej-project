package com.github.jkky_98.noteJ.domain;

import com.github.jkky_98.noteJ.domain.base.BaseEntity;
import com.github.jkky_98.noteJ.domain.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 기본 생성자
@AllArgsConstructor(access = AccessLevel.PRIVATE) // 빌더와 함께 사용할 모든 필드 생성자
public class Post extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "post_id")
    private Long id;

    private String title;

    private String content;

    private String postSummary;

    private String postUrl;

    private String thumbnail;

    private Boolean writable;

    //연관관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "series_id")
    private Series series;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder.Default
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Like> likes = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostTag> postTags = new ArrayList<>();

    public void updateUser(User user) {
        this.user = user;
    }

    // 연관관계 편의 메서드
    public void addPostTag(PostTag postTag) {
        this.postTags.add(postTag);
        postTag.updatePost(this); // 연관 관계의 주인(PostTag)에도 설정
    }

    public void removePostTag(PostTag postTag) {
        this.postTags.remove(postTag);
        postTag.updatePost(null); // 연관 관계 해제
    }

    public void addComment(Comment comment) {
        this.comments.add(comment);
        comment.updatePost(this); // 연관 관계 설정
    }

    public void removeComment(Comment comment) {
        this.comments.remove(comment);
        comment.updatePost(null); // 연관 관계 해제
    }

    public void addLike(Like like) {
        this.likes.add(like);
        like.updatePost(this); // 연관 관계 설정
    }

    public void removeLike(Like like) {
        this.likes.remove(like);
        like.updatePost(null); // 연관 관계 해제
    }
}
