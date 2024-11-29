package com.github.jkky_98.noteJ.domain;

import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.domain.user.UserRole;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class UserValidationTest {

    private Validator validator;

    @BeforeEach
    public void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("유저 엔티티 유효성 성공 시나리오 테스트")
    public void testValidUser() {
        // 유효한 User 객체 생성
        User user = User.builder()
                .username("validUsername")
                .email("valid@example.com")
                .password("validPassword123")
                .userRole(UserRole.USER)
                .build();

        // 유효성 검사
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // 검증: 유효한 객체는 violations가 비어 있어야 함
        assertThat(violations).isEmpty();

    }

    @Test
    @DisplayName("유저 엔티티 이메일 형식 실패 시나리오 테스트")
    public void testInvalidEmail() {
        // 잘못된 이메일로 User 객체 생성
        User user = User.builder()
                .username("validUsername")
                .email("invalid-email") // 잘못된 이메일 형식
                .password("validPassword123")
                .userRole(UserRole.USER)
                .build();

        // 유효성 검사
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // 검증: 이메일 필드에서 에러가 발생해야 함
        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("email");
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Email must be a valid format.");
    }

    @Test
    @DisplayName("유저 엔티티 username 형식 실패 시나리오 테스트")
    public void testInvalidUsername() {
        // 잘못된 Username으로 User 객체 생성
        User user = User.builder()
                .username("a") // 최소 길이 미만
                .email("valid@example.com")
                .password("validPassword123")
                .userRole(UserRole.USER)
                .build();

        // 유효성 검사
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // 검증: username 필드에서 에러가 발생해야 함
        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("username");
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Username must be between 2 and 20 characters");
    }

    @Test
    @DisplayName("유저 객체 비밀번호 누락 시나리오 테스트")
    public void testMissingPassword() {
        // 비밀번호 누락된 User 객체 생성
        User user = User.builder()
                .username("validUsername")
                .email("valid@example.com")
                .userRole(UserRole.USER)
                .build();

        // 유효성 검사
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // 검증: password 필드에서 에러가 발생해야 함
        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("password");
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Password is required.");
    }

    @Test
    @DisplayName("유저 객체 유저 타입 누락 시나리오 테스트")
    public void testMissingUserRole() {
        // UserRole 누락된 User 객체 생성
        User user = User.builder()
                .username("validUsername")
                .email("valid@example.com")
                .password("validPassword123")
                .build();

        // 유효성 검사
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // 검증: userRole 필드에서 에러가 발생해야 함
        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("userRole");
        assertThat(violations.iterator().next().getMessage()).isEqualTo("User role is required.");
    }


}
