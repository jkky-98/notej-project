package com.github.jkky_98.noteJ.web.controller.form;


import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class LoginFormTest {

    private Validator validator;

    @BeforeEach
    void setup() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    @DisplayName("[LoginForm] 객체 생성 및 필드 값 설정 테스트")
    void loginFormCreationAndValueTest() {
        // given
        String username = "testuser";
        String password = "password123";

        // when
        LoginForm loginForm = new LoginForm();
        loginForm.setUsername(username);
        loginForm.setPassword(password);

        // then
        assertThat(loginForm).isNotNull();
        assertThat(loginForm.getUsername()).isEqualTo(username);
        assertThat(loginForm.getPassword()).isEqualTo(password);
    }

    @Test
    @DisplayName("[LoginForm] 유효성 검증 테스트 - 성공")
    void loginFormValidationSuccessTest() {
        // given
        LoginForm loginForm = new LoginForm();
        loginForm.setUsername("testuser");
        loginForm.setPassword("password@123");

        // when
        Set<ConstraintViolation<LoginForm>> violations = validator.validate(loginForm);

        // then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("[LoginForm] 유효성 검증 테스트 - 실패 (빈 필드)")
    void loginFormValidationFailureTest() {
        // given
        LoginForm loginForm = new LoginForm();
        loginForm.setUsername(""); // 빈 값
        loginForm.setPassword(null); // null 값

        // when
        Set<ConstraintViolation<LoginForm>> violations = validator.validate(loginForm);

        // then
        assertThat(violations).hasSize(2);

        // 검증 메시지 확인 (propertyPath를 문자열로 변환)
        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactlyInAnyOrder("username", "password");
    }

    @Test
    @DisplayName("[LoginForm] 유효성 검증 테스트 - 실패 (특수문자 없음)")
    void loginFormValidationFailureNoSpecialCharacterTest() {
        // given
        LoginForm loginForm = new LoginForm();
        loginForm.setUsername("testuser");
        loginForm.setPassword("Password123"); // 특수문자가 없는 비밀번호

        // when
        Set<ConstraintViolation<LoginForm>> violations = validator.validate(loginForm);

        // then
        assertThat(violations).hasSize(1);

        // 검증 메시지 확인
        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains("Password must contain at least one special character.");
    }

    @Test
    @DisplayName("[LoginForm] 유효성 검증 테스트 - 실패 (길이 조건 미충족)")
    void loginFormValidationFailureShortPasswordTest() {
        // given
        LoginForm loginForm = new LoginForm();
        loginForm.setUsername("testuser");
        loginForm.setPassword("P@1"); // 너무 짧은 비밀번호

        // when
        Set<ConstraintViolation<LoginForm>> violations = validator.validate(loginForm);

        // then
        assertThat(violations).hasSize(1);

        // 검증 메시지 확인
        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains("Password must be at least 8 characters long.");
    }
}
