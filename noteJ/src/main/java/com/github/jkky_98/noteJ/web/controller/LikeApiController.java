package com.github.jkky_98.noteJ.web.controller;

import com.github.jkky_98.noteJ.service.LikeService;
import com.github.jkky_98.noteJ.web.controller.dto.LikeListByPostDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Slf4j
public class LikeApiController {
    private final LikeService likeService;

    @GetMapping("/post/likes")
    public ResponseEntity<List<LikeListByPostDto>> getLikesByPost(
            @RequestParam("postUrl") String postUrl
    ) {
        List<LikeListByPostDto> likeListByPostUrl = likeService.getLikeListByPostUrl(postUrl);
        return ResponseEntity.ok(likeListByPostUrl);
    }
}
