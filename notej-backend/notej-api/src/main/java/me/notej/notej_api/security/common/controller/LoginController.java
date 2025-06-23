package me.notej.notej_api.security.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {
    @GetMapping("/login")
    public String loginPage() {
        return "custom-login";  // src/main/resources/templates/custom-login.html
    }
}
