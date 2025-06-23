package me.notej.notej_api.security.credentials.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.notej.notej_api.security.credentials.dto.CredentialsLoginRequest;
import me.notej.notej_api.security.credentials.dto.CredentialsLoginResponse;
import me.notej.notej_api.security.credentials.dto.CredentialsSignUpRequest;
import me.notej.notej_api.security.credentials.service.CredentialsService;
import me.notej.notej_api.security.oauth2.util.CookieUtils;
import me.notej.notej_api.common.exception.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth/credentials")
@RequiredArgsConstructor
public class CredentialsController {

    private final CredentialsService credentialsService;

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(
            @Valid @RequestBody CredentialsSignUpRequest request,
            BindingResult bindingResult
            ) {
        ResponseEntity<ErrorResponse> BAD_REQUEST = validRequestFieldValidation(bindingResult);
        if (BAD_REQUEST != null) return BAD_REQUEST;

        // 회원가입 처리
        credentialsService.save(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("successfully signed up");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody CredentialsLoginRequest request,
            HttpServletResponse httpServletResponse,
            BindingResult bindingResult
    ) {
        ResponseEntity<ErrorResponse> BAD_REQUEST = validRequestFieldValidation(bindingResult);
        if (BAD_REQUEST != null) return BAD_REQUEST;

        CredentialsLoginResponse response = credentialsService.login(request);

        // 쿠키 주입
        CookieUtils.addCookie(httpServletResponse, "access_token", response.accessToken(), 1800); // 30분
        CookieUtils.addCookie(httpServletResponse, "refresh_token", response.refreshToken(), 2592000); // 15일
        return ResponseEntity.ok(response);
    }

    private static ResponseEntity<ErrorResponse> validRequestFieldValidation(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String details = bindingResult.getFieldErrors().stream()
                    .map(err -> err.getField() + ": " + err.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            ErrorResponse error = ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), details);

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(error);
        }
        return null;
    }
}
