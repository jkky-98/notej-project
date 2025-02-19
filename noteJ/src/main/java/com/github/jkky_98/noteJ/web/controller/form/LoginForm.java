package com.github.jkky_98.noteJ.web.controller.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class LoginForm {

    @NotBlank(message = "사용자 이름을 입력해주세요.")
    private String username;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String password;
}

