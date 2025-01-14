package com.github.jkky_98.noteJ.web.controller.form;

import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.domain.user.UserDesc;
import lombok.*;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProfileForm {

    private String username;

    private String profilePic;

    private String description;

    private String socialEmail; // nullable

    private String socialGitHub; // nullable

    private String socialTwitter; // nullable

    private String socialFacebook; // nullable

    private String socialOther; // nullable

    private int followers;

    private int followings;

    public static ProfileForm of(User user, UserDesc userDesc) {
        return ProfileForm.builder()
                .username(user.getUsername())
                .profilePic(userDesc.getProfilePic())
                .description(userDesc.getDescription())
                .socialEmail(userDesc.getSocialEmail())
                .socialGitHub(userDesc.getSocialGitHub())
                .socialTwitter(userDesc.getSocialTwitter())
                .socialFacebook(userDesc.getSocialFacebook())
                .socialOther(userDesc.getSocialOther())
                .followings(user.getFollowingList().size())
                .followers(user.getFollowerList().size())
                .build();
    }
}
