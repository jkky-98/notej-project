package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.Post;
import com.github.jkky_98.noteJ.domain.PostHits;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.repository.PostHitsRepository;
import com.github.jkky_98.noteJ.repository.PostRepository;
import com.github.jkky_98.noteJ.repository.UserRepository;
import com.github.jkky_98.noteJ.web.ClientUtils;
import com.github.jkky_98.noteJ.web.controller.dto.PostHitsDto;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PostHitsService {

    private final PostHitsRepository postHitsRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public void increamentPostView(PostHitsDto dto, HttpServletRequest request) {

        String ipAddress = ClientUtils.getRemoteIP(request);

        Post post = postRepository.findByUserUsernameAndPostUrl(dto.getUsername(), dto.getPostUrl()).orElseThrow(() -> new EntityNotFoundException("not found Post"));
        User user = userRepository.findByUsername(dto.getUsername()).orElseThrow(() -> new EntityNotFoundException("not found User"));

        PostHits postHits = PostHits.builder()
                .post(post)
                .user(user)
                .viewedAt(LocalDateTime.now())
                .ipAddress(ipAddress)
                .build();

        postHitsRepository.save(postHits);
    }
}
