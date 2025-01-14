package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.Series;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.exception.UnauthenticatedUserException;
import com.github.jkky_98.noteJ.repository.SeriesRepository;
import com.github.jkky_98.noteJ.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SeriesService {

    private final SeriesRepository seriesRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    @Transactional
    public List<Series> getSeriesByUser(User user) {
        User userFind = userRepository.findById(user.getId()).orElseThrow(() -> new EntityNotFoundException("User not found"));
        return userFind.getSeriesList();
    }
    @Transactional
    public Series getSeries(String seriesName) {
        return seriesRepository.findBySeriesName(seriesName).orElseThrow(() -> new EntityNotFoundException("Series not found"));
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
}
