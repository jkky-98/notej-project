package com.github.jkky_98.noteJ.web.controller;

import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.service.FollowService;
import com.github.jkky_98.noteJ.web.session.SessionConst;
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
                         @SessionAttribute(SessionConst.LOGIN_USER) User sessionUser,
                         @RequestHeader(value = "Referer", required = false, defaultValue = "/") String referer,
                         RedirectAttributes redirectAttributes) {
        // 팔로우 처리
        followService.follow(sessionUser.getUsername(), targetUsername);

        // 팔로우 성공 메시지를 플래시 속성에 추가
        redirectAttributes.addFlashAttribute("followSuccess", true);

        return "redirect:" + referer;
    }

    @PostMapping("/unfollow")
    public String unfollow(@RequestParam String targetUsername,
                           @SessionAttribute(SessionConst.LOGIN_USER) User sessionUser,
                           @RequestHeader(value = "Referer", required = false, defaultValue = "/") String referer,
                           RedirectAttributes redirectAttributes) {
        // 언팔로우 처리
        followService.unfollow(sessionUser.getUsername(), targetUsername);

        // 언팔로우 성공 메시지를 플래시 속성에 추가
        redirectAttributes.addFlashAttribute("unfollowSuccess", true);

        return "redirect:" + referer;
    }
}
