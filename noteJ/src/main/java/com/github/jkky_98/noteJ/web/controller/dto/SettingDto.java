package com.github.jkky_98.noteJ.web.controller.dto;

import com.github.jkky_98.noteJ.domain.user.ThemeMode;
import lombok.Data;

@Data
public class SettingDto {

    private String profilePic;

    private String blogTitle;

    private ThemeMode theme;

    private String socialEmail;

    private String socialGitHub;

    private String socialTwitter;

    private String socialFacebook;

    private String socialOther;

    private boolean commentAlarm;

    private boolean noteJAlarm;
}
