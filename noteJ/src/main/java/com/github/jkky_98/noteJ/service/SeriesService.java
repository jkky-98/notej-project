package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.Series;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.exception.UnauthenticatedUserException;
import com.github.jkky_98.noteJ.repository.PostRepository;
import com.github.jkky_98.noteJ.repository.SeriesRepository;
import com.github.jkky_98.noteJ.repository.UserRepository;
import com.github.jkky_98.noteJ.web.controller.dto.SeriesViewDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SeriesService {

    private final SeriesRepository seriesRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final ProfileService profileService;
    private final FollowService followService;
    private final PostRepository postRepository;

    @Transactional
    public List<Series> getSeriesByUser(User user) {
        User userFind = userRepository.findById(user.getId()).orElseThrow(() -> new EntityNotFoundException("User not found"));
        return userFind.getSeriesList();
    }
    @Transactional
    public Series getSeries(String seriesName, User user) {
        return seriesRepository.findBySeriesNameAndUser(seriesName, user).orElseThrow(() -> new EntityNotFoundException("Series not found"));
    }

    @Transactional
    public Series saveSeries(User sessinUser, String seriesName) {
        if (sessinUser == null) {
            throw new UnauthenticatedUserException("sessinUser is null");
        }

        User userFind = userService.findUserById(sessinUser.getId());

        // Series 엔티티 생성
        Series newSeries = Series.of(seriesName);

        userFind.addSeries(newSeries);

        return seriesRepository.save(newSeries);
    }

    @Transactional
    public void deleteSeries(String username, Long sessionUserId, String seriesName) {
        User sessionUser = userService.findUserById(sessionUserId);
        User postUser = userService.findUserByUsername(username);

        if (!sessionUser.equals(postUser)) {
            throw new RuntimeException("세션 사용자와 지우려는 시리즈의 사용자가 일치하지 않습니다.");
        }

        Series series = seriesRepository.findBySeriesNameAndUser(seriesName, postUser).orElseThrow(() -> new EntityNotFoundException("Series not found"));
        postRepository.deleteAll(series.getPosts());
        seriesRepository.delete(series);
    }
}
