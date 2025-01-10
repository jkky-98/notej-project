package com.github.jkky_98.noteJ.web.controller;

import com.github.jkky_98.noteJ.web.controller.form.LoginForm;
import com.github.jkky_98.noteJ.web.controller.form.SignUpForm;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.service.AuthService;
import com.github.jkky_98.noteJ.web.session.SessionConst;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;


@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/login")
    public String loginForm(@ModelAttribute("loginForm") LoginForm form) {
        return "auth/loginForm";
    }

    @PostMapping("/login")
    public String loginForm(@Validated @ModelAttribute("loginForm") LoginForm form,
                            BindingResult bindingResult,
                            @RequestHeader(value = "Referer", required = false) String referer,
                            HttpSession session) {

        // 유효성 검사 오류가 있으면 로그인 폼으로
        if (bindingResult.hasErrors()) {
            log.info("로그인 입력 에러");
            return "auth/loginForm";
        }

        // 로그인 처리
        try {
            User loginUser = authService.login(form);
            // 로그인 성공 시 세션에 로그인 정보 저장
            session.setAttribute(SessionConst.LOGIN_USER, loginUser);

            // Referer가 존재하면 해당 URL로 리다이렉트, 없으면 기본 페이지로 리다이렉트
            return "redirect:" + (referer != null ? referer : "/");

        } catch (AuthenticationException e) {
            // 로그인 실패 시 BindingResult에 에러 메시지 추가
            bindingResult.reject("loginFail", e.getMessage());
            return "auth/loginForm"; // 로그인 실패 시 로그인 폼으로 다시 돌아감
        }
    }

    @GetMapping("/signup")
    public String signUpForm(@ModelAttribute("signUpForm")SignUpForm form) {
        return "auth/signUpForm";
    }

    @PostMapping("/signup")
    public String signUp(@Validated @ModelAttribute("signUpForm")SignUpForm form,
                         BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "auth/signUpForm";
        }

        User signUpUser = null;
        try {
            signUpUser = authService.signUp(form);
        } catch (DataIntegrityViolationException e) {
            // 중복된 username 또는 email에 대해 bindingResult에 오류 메시지 추가
            if (e.getMessage().contains("Username")) {
                bindingResult.rejectValue("username", "error.username", "Username already exists.");
            } else if (e.getMessage().contains("Email")) {
                bindingResult.rejectValue("email", "error.email", "Email already exists.");
            }
            // 오류가 있을 경우 다시 회원가입 폼으로 리턴
            return "auth/signUpForm";
        } catch (RuntimeException e) {
            // 서비스에서 예기치 않은 예외가 발생했을 경우
            bindingResult.reject("signup.error", "예상불가능한 인증 에러가 발생중입니다. 나중에 다시 시도해주세요.");
            return "auth/signUpForm";
        }

        // 회원가입이 성공적으로 처리된 경우
        if (signUpUser == null) {
            // 회원가입 실패(아이디가 없거나 기타 이유로 null 반환)
            bindingResult.reject("signup.error", "Sign-up failed. Please try again.");
            return "auth/signUpForm";
        }

        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        authService.logout(session);
        return "redirect:/";
    }
}
