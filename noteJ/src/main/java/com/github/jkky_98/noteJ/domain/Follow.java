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
public class Follow extends BaseTimeEntity {

    @Id
    @GeneratedValue
    @Column(name = "follow_id")
    private Long id;

    //연관관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", nullable = false)
    private User follower; // 팔로우를 하는 사용자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_id", nullable = false)
    private User following; // 팔로우를 당하는 사용자

    // 팔로우 관계 생성 시에 양방향 연관관계를 설정하는 메서드
    public void updateFollower(User follower) {
        this.follower = follower;
        follower.getFollowingList().add(this);  // 팔로잉 리스트에 추가
    }

    public void updateFollowing(User following) {
        this.following = following;
        following.getFollowerList().add(this);  // 팔로워 리스트에 추가
    }

    public static Follow of(User userFollowing, User userGetFollowing) {
        return Follow.builder()
                .follower(userFollowing)
                .following(userGetFollowing)
                .build();
    }
}
