package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.Series;
import com.github.jkky_98.noteJ.domain.mapper.SeriesMapper;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.exception.UnauthenticatedUserException;
import com.github.jkky_98.noteJ.repository.PostRepository;
import com.github.jkky_98.noteJ.repository.SeriesRepository;
import com.github.jkky_98.noteJ.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("[SeriesService] Unit Test")
public class SeriesServiceTest {
    @Mock
    private SeriesRepository seriesRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @Mock
    private PostRepository postRepository;

    @Mock
    private SeriesMapper seriesMapper;

    @InjectMocks
    private SeriesService seriesService;

    private User user;
    private Series series;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testUser")
                .build();

        series = Series.builder()
                .id(10L)
                .seriesName("Test Series")
                .user(user)
                .posts(new ArrayList<>())  // 시리즈 안에 게시글 리스트 초기화
                .build();

        user.getSeriesList().add(series); // 사용자 객체에 시리즈 추가
    }

    @Test
    @DisplayName("getSeriesByUser() - 사용자의 시리즈 목록 조회")
    void testGetSeriesByUser() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        List<Series> result = seriesService.getSeriesByUser(user);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Series", result.get(0).getSeriesName());

        verify(userRepository, times(1)).findById(user.getId());
    }

    @Test
    @DisplayName("getSeriesByUser() - 존재하지 않는 사용자 ID 조회 시 예외 발생")
    void testGetSeriesByUser_UserNotFound() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> seriesService.getSeriesByUser(user));

        verify(userRepository, times(1)).findById(user.getId());
    }

    @Test
    @DisplayName("getSeries() - 특정 시리즈 조회")
    void testGetSeries() {
        when(seriesRepository.findBySeriesNameAndUser("Test Series", user)).thenReturn(Optional.of(series));

        Series result = seriesService.getSeries("Test Series", user);

        assertNotNull(result);
        assertEquals("Test Series", result.getSeriesName());

        verify(seriesRepository, times(1)).findBySeriesNameAndUser("Test Series", user);
    }

    @Test
    @DisplayName("getSeries() - 존재하지 않는 시리즈 조회 시 예외 발생")
    void testGetSeries_NotFound() {
        when(seriesRepository.findBySeriesNameAndUser("Not Found Series", user)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> seriesService.getSeries("Not Found Series", user));

        verify(seriesRepository, times(1)).findBySeriesNameAndUser("Not Found Series", user);
    }

    @Test
    @DisplayName("saveSeries() - 새로운 시리즈 저장")
    void testSaveSeries() {
        when(userService.findUserById(user.getId())).thenReturn(user);
        when(seriesMapper.toSeries("New Series")).thenReturn(series);
        when(seriesRepository.save(series)).thenReturn(series);

        Series result = seriesService.saveSeries(user, "New Series");

        assertNotNull(result);
        assertEquals("Test Series", result.getSeriesName()); // 시리즈 이름 확인
        verify(seriesRepository, times(1)).save(series);
    }

    @Test
    @DisplayName("saveSeries() - 인증되지 않은 사용자 예외 발생")
    void testSaveSeries_UnauthenticatedUser() {
        assertThrows(UnauthenticatedUserException.class, () -> seriesService.saveSeries(null, "New Series"));
    }

    @Test
    @DisplayName("deleteSeries() - 시리즈 삭제")
    void testDeleteSeries() {
        String seriesName = "Test Series";
        User sessionUser = user;
        User postUser = user;

        when(userService.findUserById(sessionUser.getId())).thenReturn(sessionUser);
        when(userService.findUserByUsername(postUser.getUsername())).thenReturn(postUser);
        when(seriesRepository.findBySeriesNameAndUser(seriesName, postUser)).thenReturn(Optional.of(series));

        seriesService.deleteSeries(postUser.getUsername(), sessionUser.getId(), seriesName);

        verify(postRepository, times(1)).deleteAll(series.getPosts());
        verify(seriesRepository, times(1)).delete(series);
    }

    @Test
    @DisplayName("deleteSeries() - 다른 사용자의 시리즈 삭제 시도 시 예외 발생")
    void testDeleteSeries_UserMismatch() {
        User sessionUser = User.builder().id(2L).username("anotherUser").build(); // 세션 유저가 다름

        when(userService.findUserById(sessionUser.getId())).thenReturn(sessionUser);
        when(userService.findUserByUsername(user.getUsername())).thenReturn(user);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> seriesService.deleteSeries(user.getUsername(), sessionUser.getId(), "Test Series"));

        assertEquals("세션 사용자와 지우려는 시리즈의 사용자가 일치하지 않습니다.", ex.getMessage());
    }

    @Test
    @DisplayName("deleteSeries() - 존재하지 않는 시리즈 삭제 시 예외 발생")
    void testDeleteSeries_NotFound() {
        String seriesName = "Not Found Series";
        User sessionUser = user;

        when(userService.findUserById(sessionUser.getId())).thenReturn(sessionUser);
        when(userService.findUserByUsername(user.getUsername())).thenReturn(user);
        when(seriesRepository.findBySeriesNameAndUser(seriesName, user)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> seriesService.deleteSeries(user.getUsername(), sessionUser.getId(), seriesName));

        verify(seriesRepository, times(1)).findBySeriesNameAndUser(seriesName, user);
    }
}
