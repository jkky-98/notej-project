package com.github.jkky_98.noteJ.web.controller.form;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UserSettingsForm {

    private MultipartFile profilePic;  // 파일 업로드를 받기 위해 MultipartFile 사용

    private String blogTitle;

    private String description;

    private String theme; // 'LIGHT', 'DARK', 'HALF'

    private String socialEmail; // nullable

    private String socialGitHub; // nullable

    private String socialTwitter; // nullable

    private String socialFacebook; // nullable

    private String socialOther; // nullable

    private boolean commentAlarm;

    private boolean noteJAlarm;
}
