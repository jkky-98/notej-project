package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.repository.TagRepository;
import com.github.jkky_98.noteJ.repository.UserRepository;
import com.github.jkky_98.noteJ.web.controller.dto.TagCountDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {

    private final UserService userService;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<TagCountDto> getAllTag(String username) {
        User userFind = userService.findUserByUsername(username);

        return userRepository.findTagsByUser(userFind.getUsername());
    }
}
