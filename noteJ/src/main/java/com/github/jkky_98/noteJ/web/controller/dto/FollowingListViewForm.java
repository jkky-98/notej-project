package com.github.jkky_98.noteJ.web.controller.dto;

import com.github.jkky_98.noteJ.domain.user.User;
import lombok.*;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FollowingListViewForm {
    private String followingUserUsername;
    private String followingUserDescription;
    private String followingUserProfilePic;

    public static FollowingListViewForm of(User following) {
        return FollowingListViewForm.builder()
                .followingUserDescription(following.getUserDesc().getDescription())
                .followingUserProfilePic(following.getUserDesc().getProfilePic())
                .followingUserUsername(following.getUsername())
                .build();
    }
}
