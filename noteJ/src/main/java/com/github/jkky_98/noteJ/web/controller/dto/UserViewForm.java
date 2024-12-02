package com.github.jkky_98.noteJ.web.controller.dto;

import com.github.jkky_98.noteJ.domain.user.UserDesc;
import lombok.Data;

@Data
public class UserViewForm {

    private String username;
    private String email;
    private String userDesc;
    private String profilePic;
    private String blogTitle;

}
