package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.domain.user.UserDesc;
import com.github.jkky_98.noteJ.web.controller.form.ProfileForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserService userService;

    @Transactional(readOnly = true)
    public ProfileForm getProfile(Long userId) {
        // 사용자 정보를 조회
        User userFind = userService.findUserById(userId);

        UserDesc userDesc = userFind.getUserDesc();

        return ProfileForm.of(userFind, userDesc);

    }
}
