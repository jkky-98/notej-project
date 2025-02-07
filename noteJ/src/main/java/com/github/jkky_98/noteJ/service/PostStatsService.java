package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.web.controller.dto.PostStatsForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostStatsService {

    private final UserService userService;

    public PostStatsForm getPostStats(String postUrl, User sessionUser) {
        // DTO 반환
        PostStatsForm postStatsForm = new PostStatsForm();

        return postStatsForm;
    }

}
