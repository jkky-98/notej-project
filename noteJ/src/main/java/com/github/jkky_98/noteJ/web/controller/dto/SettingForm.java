package com.github.jkky_98.noteJ.web.controller.dto;

import com.github.jkky_98.noteJ.domain.user.ThemeMode;
import com.github.jkky_98.noteJ.domain.user.UserDesc;
import lombok.*;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SettingForm {

    private String description;

    private String profilePic;

    private String blogTitle;

    private ThemeMode theme;

    private String socialEmail; // nullable

    private String socialGitHub; // nullable

    private String socialTwitter; // nullable

    private String socialFacebook; // nullable

    private String socialOther; // nullable

    private boolean commentAlarm;

    private boolean noteJAlarm;

    public static SettingForm ofFromUserDesc(UserDesc userDesc) {
        return SettingForm.builder()
                .description(userDesc.getDescription())
                .profilePic(userDesc.getProfilePic())
                .blogTitle(userDesc.getBlogTitle())
                .theme(userDesc.getTheme())
                .socialEmail(userDesc.getSocialEmail())
                .socialGitHub(userDesc.getSocialGitHub())
                .socialTwitter(userDesc.getSocialTwitter())
                .socialFacebook(userDesc.getSocialFacebook())
                .socialOther(userDesc.getSocialOther())
                .commentAlarm(userDesc.isCommentAlarm())
                .noteJAlarm(userDesc.isNoteJAlarm())
                .build();
    }
}
