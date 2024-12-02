package com.github.jkky_98.noteJ.web.controller.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;


@Data
public class LoginForm {

    @NotEmpty
    private String username;

    @NotEmpty
    private String password;
}
