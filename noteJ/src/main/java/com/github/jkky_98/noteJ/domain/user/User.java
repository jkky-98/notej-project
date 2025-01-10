package com.github.jkky_98.noteJ.domain.user;

import com.github.jkky_98.noteJ.domain.*;
import com.github.jkky_98.noteJ.domain.base.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 기본 생성자
@AllArgsConstructor(access = AccessLevel.PRIVATE) // 빌더와 함께 사용할 모든 필드 생성자
@Table(name = "user_table")
public class User extends BaseEntity {
    @Id @GeneratedValue
    @Column(name = "user_id")
    private Long id;

    private String username;

    @Column(unique = true)
    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    // 연관관계
    @JoinColumn(name = "user_desc_id")
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private UserDesc userDesc;

    @Builder.Default
    @OneToMany(mappedBy = "user")
    private List<Post> posts = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "user")
    private List<Series> seriesList = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "user")
    private List<Notification> receivedNotifications = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Like> likes = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "follower", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Follow> followingList = new ArrayList<>(); // 내가 팔로우한 사용자들

    @Builder.Default
    @OneToMany(mappedBy = "following", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Follow> followerList = new ArrayList<>(); // 나를 팔로우한 사용자들

    @Builder.Default
    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Notification> sentNotifications = new ArrayList<>();

    public void updateUserDesc(UserDesc userDesc) {
        this.userDesc = userDesc;
    }

    public void addPost(Post post) {
        this.posts.add(post);
        post.updateUser(this);
    }

    public void addSeries(Series series) {
        this.seriesList.add(series);
        series.updateUser(this);
    }

    // 연관관계 설정 메서드 (User가 Notification을 수신하는 경우)
    public void addReceivedNotification(Notification notification) {
        this.receivedNotifications.add(notification);
        if (notification.getUser() != this) {
            notification.updateUser(this);  // 수신자 설정
        }
    }

    // 연관관계 설정 메서드 (User가 Notification을 발송하는 경우)
    public void addSentNotification(Notification notification) {
        this.sentNotifications.add(notification);
        if (notification.getSender() != this) {
            notification.updateUser(this);  // 발송자 설정
        }
    }

}
