package com.github.jkky_98.noteJ.web.controller;

import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.service.LikeService;
import com.github.jkky_98.noteJ.service.dto.DeleteLikeToServiceDto;
import com.github.jkky_98.noteJ.service.dto.GetLikeStatusToServiceDto;
import com.github.jkky_98.noteJ.service.dto.SaveLikeToServiceDto;
import com.github.jkky_98.noteJ.web.controller.form.LikeCardForm;
import com.github.jkky_98.noteJ.web.controller.form.LikeDeleteRequestForm;
import com.github.jkky_98.noteJ.web.controller.form.LikeSaveRequestForm;
import com.github.jkky_98.noteJ.web.controller.dto.LikeStatusForm;
import com.github.jkky_98.noteJ.web.session.SessionConst;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/like")
public class LikeController {

    private final LikeService likeService;

    @GetMapping("/status")
    @ResponseBody
    public LikeStatusForm getLikeStatus(
            @RequestParam String postUrl,
            @SessionAttribute(SessionConst.LOGIN_USER) User sessionUser
    ) {
        LikeStatusForm likeStatus = likeService.getLikeStatus(
                postUrl,
                sessionUser.getId()
        );

        return likeStatus;
    }

    @PostMapping("/save")
    @ResponseBody
    public ResponseEntity<?> saveLike(
            @RequestBody LikeSaveRequestForm form,
            @SessionAttribute(SessionConst.LOGIN_USER) User sessionUser
    ) {
        SaveLikeToServiceDto dto = new SaveLikeToServiceDto(form.getPostUrl(), form.isLiked(), sessionUser.getId());
        likeService.saveLike(dto);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteLike(
            @RequestBody LikeDeleteRequestForm form,
            @SessionAttribute(SessionConst.LOGIN_USER) User sessionUser
    ) {
        DeleteLikeToServiceDto dto = new DeleteLikeToServiceDto(form.getPostUrl(), form.isLiked(), sessionUser.getId());
        likeService.deleteLike(dto);

        return ResponseEntity.ok().build();
    }

    @GetMapping("")
    public String getLike(
            @SessionAttribute(name = SessionConst.LOGIN_USER) User sessionUser,
            Model model
    ) {
        List<LikeCardForm> likeCards = likeService.getLikeCards(sessionUser.getId());
        model.addAttribute("likeCards", likeCards);
        return "userLike";
    }
}
