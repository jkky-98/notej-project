package com.github.jkky_98.noteJ.web.controller.dto;

import com.github.jkky_98.noteJ.domain.user.User;
import lombok.*;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FollowListPostProfileForm {
    private String username;
    private String profilePic;
    private Integer followingCount;
    private Integer followerCount;

    public static FollowListPostProfileForm ofFollowing(User user) {
        return FollowListPostProfileForm.builder()
                .username(user.getUsername())
                .profilePic(user.getUserDesc().getProfilePic())
                .followingCount(user.getFollowingList().size())
                .build();
    }

    public static FollowListPostProfileForm ofFollower(User user) {
        return FollowListPostProfileForm.builder()
                .username(user.getUsername())
                .profilePic(user.getUserDesc().getProfilePic())
                .followerCount(user.getFollowerList().size())
                .build();
    }
}
