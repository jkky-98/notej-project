package com.github.jkky_98.noteJ.web.controller.form;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignUpForm {

    @NotBlank(message = "사용자 이름은 필수 입력 항목입니다.")
    @Size(min = 2, max = 20, message = "사용자 이름은 2자 이상 20자 이내여야 합니다.")
    private String username;

    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @Column(unique = true)
    private String email;

    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @Size(min = 8, max = 100, message = "비밀번호는 최소 8자 이상 입력해야 합니다.")
    @Pattern(regexp = ".*[!@#$%^&*(),.?\":{}|<>].*", message = "비밀번호는 최소 하나의 특수문자를 포함해야 합니다.")
    private String password;

    @NotBlank(message = "블로그 제목은 필수 입력 항목입니다.")
    @Size(min = 2, max = 20, message = "블로그 제목은 2자 이상 20자 이내여야 합니다.")
    private String blogTitle;

}
