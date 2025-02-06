package com.github.jkky_98.noteJ.web.controller;

import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.service.LikeService;
import com.github.jkky_98.noteJ.service.dto.DeleteLikeToServiceDto;
import com.github.jkky_98.noteJ.service.dto.GetLikeStatusToServiceDto;
import com.github.jkky_98.noteJ.service.dto.SaveLikeToServiceDto;
import com.github.jkky_98.noteJ.web.controller.form.LikeDeleteRequestForm;
import com.github.jkky_98.noteJ.web.controller.form.LikeSaveRequestForm;
import com.github.jkky_98.noteJ.web.controller.dto.LikeStatusForm;
import com.github.jkky_98.noteJ.web.session.SessionConst;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/like")
public class LikeController {

    private final LikeService likeService;

    @GetMapping("/status")
    public LikeStatusForm getLikeStatus(
            @RequestParam String postUrl,
            @SessionAttribute(SessionConst.LOGIN_USER) User sessionUser
    ) {
        LikeStatusForm likeStatus = likeService.getLikeStatus(
                new GetLikeStatusToServiceDto(
                        postUrl,
                        sessionUser.getId()
                )
        );

        return likeStatus;
    }

    @PostMapping("/save")
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
}
