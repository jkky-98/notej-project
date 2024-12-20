package com.github.jkky_98.noteJ.web.controller.dto;

import com.github.jkky_98.noteJ.domain.user.ThemeMode;
import lombok.Data;

@Data
public class SettingDto {

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
}
