package com.github.jkky_98.noteJ.web.controller.dto;

import com.github.jkky_98.noteJ.domain.user.User;
import lombok.*;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FollowListPostProfileDto {
    private String username;
    private String profilePic;
    private Integer followingCount;
    private Integer followerCount;

    public static FollowListPostProfileDto ofFollowing(User user) {
        return FollowListPostProfileDto.builder()
                .username(user.getUsername())
                .profilePic(user.getUserDesc().getProfilePic())
                .followingCount(user.getFollowingList().size())
                .build();
    }

    public static FollowListPostProfileDto ofFollower(User user) {
        return FollowListPostProfileDto.builder()
                .username(user.getUsername())
                .profilePic(user.getUserDesc().getProfilePic())
                .followerCount(user.getFollowerList().size())
                .build();
    }
}
