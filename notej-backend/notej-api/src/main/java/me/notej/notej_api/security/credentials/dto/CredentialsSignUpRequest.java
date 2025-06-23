package me.notej.notej_api.security.credentials.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CredentialsSignUpRequest(
        @NotBlank(message = "이메일을 입력해주세요")
        @Email(message = "유효한 이메일 주소여야 합니다")
        String email,

        @NotBlank(message = "비밀번호를 입력해주세요")
        @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다")
        String password,

        @NotBlank(message = "비밀번호 확인을 입력해주세요")
        String confirmPassword,

        @NotBlank(message = "이름을 입력해주세요")
        String name
) {
}
