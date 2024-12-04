package com.github.jkky_98.noteJ.web.controller;

import com.github.jkky_98.noteJ.web.controller.form.LoginForm;
import com.github.jkky_98.noteJ.web.controller.form.SignUpForm;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.service.AuthService;
import com.github.jkky_98.noteJ.web.session.SessionConst;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final ApplicationContext applicationContext;

    @GetMapping("/login")
    public String loginForm(@ModelAttribute("loginForm") LoginForm form) {
        return "auth/loginForm";
    }

    @PostMapping("/login")
    public String loginForm(@Validated @ModelAttribute("loginForm") LoginForm form,
                            BindingResult bindingResult,
                            @RequestParam(defaultValue = "/") String redirectURL,
                            HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            log.info("LogIn error");
            List<ObjectError> allErrors = bindingResult.getAllErrors();
            for (ObjectError allError : allErrors) {
                log.info("Error : {}", allError);
            }
            return "auth/loginForm";
        }

        User loginUser = authService.login(form);

        if (loginUser == null) {
            bindingResult.reject("loginFail", "아이디 또는 비밀번호가 맞지 않습니다.");
            return "auth/loginForm";
        }

        // 성공 로직
        HttpSession session = request.getSession();

        session.setAttribute(SessionConst.LOGIN_USER, loginUser);

        return "redirect:" + redirectURL;
    }

    @GetMapping("/signup")
    public String signUpForm(@ModelAttribute("signUpForm")SignUpForm form) {
        return "auth/signUpForm";
    }

    @PostMapping("/signup")
    public String signUp(@Validated @ModelAttribute("signUpForm")SignUpForm form,
                         BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            log.info("SignUp error");
            return "auth/signUpForm";
        }

        User signUpUser = authService.signUp(form, bindingResult);

        if (signUpUser == null) {
            return "auth/signUpForm";
        }
        ConfigurableEnvironment environment = (ConfigurableEnvironment) applicationContext.getEnvironment();
        log.info("정상로직 프로필 : {}", (Object) environment.getActiveProfiles());

        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        authService.logout(session);
        return "redirect:/";
    }
}
