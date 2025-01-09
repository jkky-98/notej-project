package com.github.jkky_98.noteJ.web.controller;

import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PostMapping("/follow")
    public String follow(@RequestParam String targetUsername,
                         @SessionAttribute("loginUser") User loginUser,
                         @RequestHeader(value = "Referer", required = false) String referer,
                         RedirectAttributes redirectAttributes) {
        // 팔로우 처리
        followService.follow(loginUser.getUsername(), targetUsername);

        // 팔로우 성공 메시지를 플래시 속성에 추가
        redirectAttributes.addFlashAttribute("followSuccess", true);

        // Referer가 있으면 해당 URL로 리다이렉트, 없으면 기본 페이지로 리다이렉트
        if (referer != null && !referer.isEmpty()) {
            return "redirect:" + referer;
        } else {
            return "redirect:/";  // Referer가 없으면 기본 페이지로 리다이렉트
        }
    }
}
