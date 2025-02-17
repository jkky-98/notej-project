package com.github.jkky_98.noteJ.web.controller;

import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.service.xterms.CommandDispatcher;
import com.github.jkky_98.noteJ.web.controller.dto.XtermsReqeustDto;
import com.github.jkky_98.noteJ.web.controller.dto.XtermsResponseDto;
import com.github.jkky_98.noteJ.web.session.SessionConst;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
public class XTermsController {

    private final CommandDispatcher commandDispatcher;

    @PostMapping("/api/execute-command")
    public ResponseEntity<?> execute(
            @SessionAttribute(SessionConst.LOGIN_USER) User sessionUser,
            @RequestBody XtermsReqeustDto dto
            ) {

        String cmd = dto.getCommand();
        Long sessionUserId = sessionUser.getId();

        log.info("Received command: {} from userId: {}", cmd, sessionUserId);

        XtermsResponseDto responseDto = commandDispatcher.dispatch(cmd, sessionUserId);

        log.info("Response: {}", responseDto.getResult());

        return ResponseEntity.ok(responseDto);
    }
}
