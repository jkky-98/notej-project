package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.Post;
import com.github.jkky_98.noteJ.domain.PostHits;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.repository.PostHitsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostHitsService {

    private final PostHitsRepository postHitsRepository;
    private final PostService postService;

    @Transactional
    public void increamentPostView(String usernamePost, String postUrl, Optional<User> sessionUser, String clientIp) {

        Post postFind = postService.findByUserUsernameAndPostUrl(usernamePost, postUrl);

        sessionUser.ifPresentOrElse(
                user -> {
                    // 세션에 로그인된 사용자가 있고, 그 사용자가 요청한 게시글의 작성자와 같지 않으면 조회수 증가
                    if (!user.getUsername().equals(usernamePost)) {
                        savePostHit(user, postFind, clientIp);
                    }
                },
                () -> {
                    // 세션에 로그인된 사용자가 없으면 조회수 증가
                    savePostHit(null, postFind, clientIp);
                }
        );
    }

    private void savePostHit(User user, Post post, String clientIp) {
        PostHits postHits = PostHits.builder()
                .viewedAt(LocalDateTime.now())
                .ipAddress(clientIp)
                .post(post)
                .user(user) // user는 null일 수 있음
                .build();

        postHitsRepository.save(postHits);
    }
}
