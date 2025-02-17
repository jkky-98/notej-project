package com.github.jkky_98.noteJ.service.xterms;

import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.repository.LikeRepository;
import com.github.jkky_98.noteJ.service.LikeService;
import com.github.jkky_98.noteJ.service.UserService;
import com.github.jkky_98.noteJ.web.controller.dto.XtermsReqeustDto;
import com.github.jkky_98.noteJ.web.controller.dto.XtermsResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class XtermsService {

    private final LikeRepository likeRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public String getLikeAll(
            String command, Long userId
    ) {

        User user = userService.findUserById(userId);

        long countAllLike = likeRepository.countLikesByUserId(userId);

        return user.getUsername() + "님이 모은 총 좋아요 개수 : " + countAllLike + "개 💖";
    }

    @Transactional(readOnly = true)
    public String getLikeAllBySeries(String cmd, Long userId, String seriesName) {
        User user = userService.findUserById(userId);

        long countAllLikeBySeries = likeRepository.countLikesByUserIdAndSeriesName(userId, seriesName);

        return user.getUsername() + "님의 시리즈 : '" +  seriesName + "'의 총 좋아요 개수 : " + countAllLikeBySeries + "개 💖";
    }
}
