package com.github.jkky_98.noteJ.web.controller.form;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class SignUpFormTest {
    private Validator validator;

    @BeforeEach
    void setup() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    @DisplayName("[SignUpForm] 객체 생성 및 필드 값 설정 테스트")
    void signUpFormCreationAndValueTest() {
        // given
        String username = "testuser";
        String email = "test@example.com";
        String password = "password123";
        String blogTitle = "My Blog";

        // when
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setUsername(username);
        signUpForm.setEmail(email);
        signUpForm.setPassword(password);
        signUpForm.setBlogTitle(blogTitle);

        // then
        assertThat(signUpForm).isNotNull();
        assertThat(signUpForm.getUsername()).isEqualTo(username);
        assertThat(signUpForm.getEmail()).isEqualTo(email);
        assertThat(signUpForm.getPassword()).isEqualTo(password);
        assertThat(signUpForm.getBlogTitle()).isEqualTo(blogTitle);
    }

    @Test
    @DisplayName("[SignUpForm] 유효성 검증 테스트 - 성공")
    void signUpFormValidationSuccessTest() {
        // given
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setUsername("testuser");
        signUpForm.setEmail("test@example.com");
        signUpForm.setPassword("password123");
        signUpForm.setBlogTitle("My Blog");

        // when
        Set<ConstraintViolation<SignUpForm>> violations = validator.validate(signUpForm);

        // then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("[SignUpForm] 유효성 검증 테스트 - 실패 (빈 값 및 형식 오류)")
    void signUpFormValidationFailureTest() {
        // given
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setUsername("");
        signUpForm.setEmail("invalid-email");
        signUpForm.setPassword("123");
        signUpForm.setBlogTitle("");

        // when
        Set<ConstraintViolation<SignUpForm>> violations = validator.validate(signUpForm);

        // then
        assertThat(violations).hasSize(6); // 6개의 검증 실패 메시지

        // 필드별 검증 실패 메시지 확인
        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactlyInAnyOrder("username", "username", "email", "password", "blogTitle", "blogTitle");

        // 검증 메시지의 내용 확인 (선택적)
        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains(
                        "Username is required.",
                        "Username must be between 2 and 20 characters",
                        "Email must be a valid format.",
                        "Password must be at least 8 characters long.",
                        "BlogTitle is required.",
                        "Blog title must be between 2 and 20 characters"
                );
    }
}
