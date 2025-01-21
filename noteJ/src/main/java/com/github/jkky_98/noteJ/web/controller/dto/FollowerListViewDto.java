package com.github.jkky_98.noteJ.web.controller.dto;

import com.github.jkky_98.noteJ.domain.user.User;
import lombok.*;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FollowerListViewDto {
    private String followerUserUsername;
    private String followerUserDescription;
    private String followerUserProfilePic;
    private boolean followStatus;

    public static FollowerListViewDto of(User follower) {
        return FollowerListViewDto.builder()
                .followerUserDescription(follower.getUserDesc().getDescription())
                .followerUserProfilePic(follower.getUserDesc().getProfilePic())
                .followerUserUsername(follower.getUsername())
                .build();
    }
}
