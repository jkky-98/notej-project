package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.mapper.GlobalMapper;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.domain.user.UserDesc;
import com.github.jkky_98.noteJ.repository.UserRepository;
import com.github.jkky_98.noteJ.web.controller.form.UserNavigationViewForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GlobalService {

    private final GlobalMapper globalMapper;
    private final UserService userService;

    @Transactional(readOnly = true)
    @Cacheable(value = "sessionUserInfoCache", key = "#userId", unless = "#result == null")
    public UserNavigationViewForm getNavigationWithSessionUser(Long userId) {

        User user = userService.findUserById(userId);

        return globalMapper.toUserNavigationViewForm(user);
    }
}
