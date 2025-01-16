package com.github.jkky_98.noteJ.web.controller;

import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.service.LikeService;
import com.github.jkky_98.noteJ.service.dto.GetLikeStatusServiceDto;
import com.github.jkky_98.noteJ.web.controller.dto.LikeRequestDto;
import com.github.jkky_98.noteJ.web.controller.dto.LikeStatusResponseDto;
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
    public LikeStatusResponseDto getLikeStatus(
            @RequestParam String postUrl,
            @SessionAttribute(SessionConst.LOGIN_USER) User sessionUser
    ) {
        LikeStatusResponseDto likeStatus = likeService.getLikeStatus(
                new GetLikeStatusServiceDto(
                        postUrl,
                        sessionUser.getId()
                )
        );

        return likeStatus;
    }

    @PostMapping("/save")
    public ResponseEntity<?> saveLike(
            @RequestBody LikeRequestDto data,
            @SessionAttribute(SessionConst.LOGIN_USER) User sessionUser
    ) {
        likeService.saveLike(data.getPostUrl(), data.isLiked(), sessionUser.getId());

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteLike(
            @RequestBody LikeRequestDto data,
            @SessionAttribute(SessionConst.LOGIN_USER) User sessionUser
    ) {
        likeService.deleteLike(data.getPostUrl(), data.isLiked(), sessionUser.getId());

        return ResponseEntity.ok().build();
    }
}
