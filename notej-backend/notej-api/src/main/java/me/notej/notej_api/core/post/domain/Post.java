package me.notej.notej_api.core.post.domain;

import jakarta.persistence.*;
import lombok.*;
import me.notej.notej_api.core.blogmain.domain.Blog;
import me.notej.notej_api.core.category.domain.Category;
import me.notej.notej_api.core.comment.domain.Comment;
import me.notej.notej_api.global.baseentity.BaseTimeEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "post")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Post extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String bio;

    private boolean active;

    private String thumbnail;

    @OneToMany(mappedBy = "post")
    @Builder.Default
    private List<PostTag> postTags = new ArrayList<>();

    @OneToMany(mappedBy = "post")
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_id")
    private Blog blog;

    public void addPostTag(PostTag postTag) {
        postTags.add(postTag);
        postTag.setPost(this);
    }

}
