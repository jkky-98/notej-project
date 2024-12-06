package com.github.jkky_98.noteJ.web.controller.form;

import lombok.Data;

@Data
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


}
