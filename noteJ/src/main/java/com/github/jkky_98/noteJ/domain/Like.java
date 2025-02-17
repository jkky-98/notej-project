package com.github.jkky_98.noteJ.domain;

import com.github.jkky_98.noteJ.domain.base.BaseTimeEntity;
import com.github.jkky_98.noteJ.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 기본 생성자
@AllArgsConstructor(access = AccessLevel.PRIVATE) // 빌더와 함께 사용할 모든 필드 생성자
@Table(name = "likes")
public class Like extends BaseTimeEntity {

    @Id
    @GeneratedValue
    @Column(name = "like_id")
    private Long id;

    //연관관계
    @JoinColumn(name = "post_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Post post;

    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @JoinColumn(name = "user_get_like_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private User userGetLike;

    public void updatePost(Post post) {
        this.post = post;
    }

    public static Like of(Post post, User user) {
        return Like.builder()
                .post(post)
                .user(user)
                .userGetLike(post.getUser())
                .build();
    }
}
