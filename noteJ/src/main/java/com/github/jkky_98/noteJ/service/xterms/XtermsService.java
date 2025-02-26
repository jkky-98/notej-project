package com.github.jkky_98.noteJ.service.xterms;

import com.github.jkky_98.noteJ.domain.Series;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.repository.LikeRepository;
import com.github.jkky_98.noteJ.service.LikeService;
import com.github.jkky_98.noteJ.service.SeriesService;
import com.github.jkky_98.noteJ.service.UserService;
import com.github.jkky_98.noteJ.web.controller.dto.XtermsReqeustDto;
import com.github.jkky_98.noteJ.web.controller.dto.XtermsResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class XtermsService {

    private final LikeRepository likeRepository;
    private final UserService userService;
    private final SeriesService seriesService;

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
    @Transactional
    public String changePostsSeries(Long userId, String oldSeriesName, String newSeriesName) {
        User user = userService.findUserById(userId);

        // newSeriesName 존재한다면
        Optional<Series> existingSeries = findSeriesByName(newSeriesName, user);
        if (existingSeries.isPresent()) {
            Series newSeries = existingSeries.get();
            Series oldSeries = seriesService.getSeries(oldSeriesName, user);

            oldSeries.getPosts()
                    .forEach(post -> post.updateSeries(newSeries));
        } else {
            Series newSeries = seriesService.saveSeries(user, newSeriesName);
            Series oldSeries = seriesService.getSeries(oldSeriesName, user);

            oldSeries.getPosts()
                    .forEach(post -> post.updateSeries(newSeries));
        }

        return "✅ [시리즈 " + oldSeriesName + "] 에 소속된 모든 게시글을 " + "[시리즈 " + newSeriesName + "] 으로 변경했습니다.";
    }

    private static Optional<Series> findSeriesByName(String newSeriesName, User user) {
        return user.getSeriesList().stream()
                .filter(series -> series.getSeriesName().equals(newSeriesName))
                .findFirst();
    }

}
