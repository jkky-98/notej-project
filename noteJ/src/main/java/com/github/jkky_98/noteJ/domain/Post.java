package com.github.jkky_98.noteJ.domain;

import com.github.jkky_98.noteJ.domain.base.BaseEntity;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.web.controller.dto.AutoEditPostRequest;
import com.github.jkky_98.noteJ.web.controller.form.WriteForm;
import jakarta.persistence.*;
import lombok.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.github.jkky_98.noteJ.service.util.DefaultConst.DEFAULT_POST_PIC;

@Entity
@Table(
        name = "POST",
        indexes = @Index(name = "idx_post_url", columnList = "postUrl")
)
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 기본 생성자
@AllArgsConstructor(access = AccessLevel.PRIVATE) // 빌더와 함께 사용할 모든 필드 생성자
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id", nullable = false)
    private Long id;

    @Column(nullable = false, length = 50)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    private String postSummary;

    @Column(unique = true, nullable = false)
    private String postUrl;

    @Column(nullable = false)
    private String thumbnail;

    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean writable;

    @Version
    private Integer version;

    @Builder.Default
    @Column(nullable = false)
    private int viewCount = 0;

    //연관관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "series_id", nullable = false)
    private Series series;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
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

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PostFile> postFiles = new ArrayList<>();

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

    public void updateSeries(Series series) {
        this.series = series;
        series.getPosts().add(this);
    }

    public void increaseViewCount() {
        viewCount++;
    }

    // updateThumbnail 따로 필요
    // updateSeries 따로 필요
    public void updatePostWithoutThumbnailAndSeries(WriteForm form) {
        title = form.getTitle();
        content = form.getContent();
        postSummary = form.getPostSummary();
        writable = form.isOpen();
    }

    public void updateThumbnail(String storedFileName) {
        this.thumbnail = storedFileName;
    }

    public void updateEditPostTemp(AutoEditPostRequest request, Series series) {
        title = request.getTitle();
        content = decodingContent(request.getContent());
        postUrl = request.getTitle() + UUID.randomUUID();
        this.series = series;
        thumbnail = DEFAULT_POST_PIC;
    }

    private static String decodingContent(String content) {
        return URLDecoder.decode(content, StandardCharsets.UTF_8);
    }
}
