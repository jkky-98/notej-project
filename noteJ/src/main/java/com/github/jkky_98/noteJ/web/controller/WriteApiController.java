package com.github.jkky_98.noteJ.web.controller;

import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.service.WriteService;
import com.github.jkky_98.noteJ.web.controller.dto.AutoEditPostRequest;
import com.github.jkky_98.noteJ.web.controller.dto.AutoEditPostResponse;
import com.github.jkky_98.noteJ.web.controller.dto.AutoSavePostRequest;
import com.github.jkky_98.noteJ.web.controller.dto.AutoSavePostResponse;
import com.github.jkky_98.noteJ.web.session.SessionConst;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/write")
public class WriteApiController {

    private final WriteService writeService;

    @PostMapping("/auto-save")
    public ResponseEntity<?> autoSavePost(
            @RequestBody AutoSavePostRequest autoSaveRequest,
            @SessionAttribute(SessionConst.LOGIN_USER) User sessionUser
    ) throws IOException {
        AutoSavePostResponse autoSavePostResponse = writeService.autoSavePost(autoSaveRequest, sessionUser.getId());

        return ResponseEntity.ok(autoSavePostResponse);
    }

    @PostMapping("/auto-edit")
    public ResponseEntity<?> autoEditPost(
            @RequestBody AutoEditPostRequest autoEditPostRequest,
            @SessionAttribute(SessionConst.LOGIN_USER) User sessionUser
    ) throws IOException {
        AutoEditPostResponse autoEditPostResponse = writeService.autoEditPost(autoEditPostRequest, sessionUser.getId());
        return ResponseEntity.ok(autoEditPostResponse);
    }
}
