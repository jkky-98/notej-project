package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.Post;
import com.github.jkky_98.noteJ.domain.Series;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.repository.LikeRepository;
import com.github.jkky_98.noteJ.service.xterms.XtermsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@DisplayName("[XtermsServiceTest] Unit Tests")
public class XtermsServiceTest {

    @Mock
    private LikeRepository likeRepository;
    @Mock
    private UserService userService;
    @Mock
    private SeriesService seriesService;

    @InjectMocks
    private XtermsService xtermsService;

    private User user;

    @BeforeEach
    void setUp() {
        // 테스트용 User 생성 (빌더 패턴 또는 생성자 사용)
        user = User.builder()
                .id(1L)
                .username("testUser")
                .build();
    }

    @Test
    @DisplayName("getLikeAll: 총 좋아요 개수 반환")
    void testGetLikeAll() {
        Long userId = 1L;
        String command = "dummyCommand"; // command 인자는 현재 사용되지 않음
        long countLikes = 100L;

        when(userService.findUserById(userId)).thenReturn(user);
        when(likeRepository.countLikesByUserId(userId)).thenReturn(countLikes);

        String result = xtermsService.getLikeAll(command, userId);
        String expected = "testUser님이 모은 총 좋아요 개수 : 100개 💖";

        assertThat(expected).isEqualTo(result);
        verify(userService).findUserById(userId);
        verify(likeRepository).countLikesByUserId(userId);
    }

    @Test
    @DisplayName("getLikeAllBySeries: 시리즈별 총 좋아요 개수 반환")
    void testGetLikeAllBySeries() {
        Long userId = 1L;
        String cmd = "dummyCommand";
        String seriesName = "MySeries";
        long countLikesBySeries = 50L;

        when(userService.findUserById(userId)).thenReturn(user);
        when(likeRepository.countLikesByUserIdAndSeriesName(userId, seriesName)).thenReturn(countLikesBySeries);

        String result = xtermsService.getLikeAllBySeries(cmd, userId, seriesName);
        String expected = "testUser님의 시리즈 : 'MySeries'의 총 좋아요 개수 : 50개 💖";

        assertThat(expected).isEqualTo(result);
        verify(userService).findUserById(userId);
        verify(likeRepository).countLikesByUserIdAndSeriesName(userId, seriesName);
    }

    @Test
    @DisplayName("changePostsSeries() - 시리즈1에 소속된 모든 게시글 새로운 시리즈2로 수정 (시리즈2 이미 존재 경우)")
    void testChangePostsSeriesExisted() {
        Long userId = 1L;
        String oldSeriesName = "MySeries";
        String newSeriesName = "MySeries2";

        User sessionUser = User.builder()
                        .id(1L)
                        .build();

        Series newSeries = Series.of(newSeriesName);
        Series oldSeries = Series.of(oldSeriesName);

        oldSeries.getPosts().add(
                Post.builder()
                        .series(oldSeries)
                        .build()
        );

        sessionUser.getSeriesList().add(
                newSeries
        );

        when(userService.findUserById(userId)).thenReturn(sessionUser);
        when(seriesService.getSeries(oldSeriesName, sessionUser)).thenReturn(newSeries);

        String resultString = xtermsService.changePostsSeries(1L, oldSeriesName, newSeriesName);
        String expectedString = "✅ [시리즈 " + oldSeriesName + "] 에 소속된 모든 게시글을 " + "[시리즈 " + newSeriesName + "] 으로 변경했습니다.";

        assertThat(resultString).isEqualTo(expectedString);
        verify(userService).findUserById(userId);
        verify(seriesService).getSeries(oldSeriesName, sessionUser);

    }

    @Test
    @DisplayName("changePostsSeries() - 시리즈1에 소속된 모든 게시글 새로운 시리즈2로 수정 (시리즈2 존재하지 않음)")
    void testChangePostsSeriesNotExisted() {
        Long userId = 1L;
        String oldSeriesName = "MySeries";
        String newSeriesName = "MySeries2";

        User sessionUser = User.builder()
                .id(1L)
                .build();

        Series newSeries = Series.of(newSeriesName);
        Series oldSeries = Series.of(oldSeriesName);

        oldSeries.getPosts().add(
                Post.builder()
                        .series(oldSeries)
                        .build()
        );

        when(userService.findUserById(userId)).thenReturn(sessionUser);
        when(seriesService.saveSeries(sessionUser, newSeriesName)).thenReturn(newSeries);
        when(seriesService.getSeries(oldSeriesName, sessionUser)).thenReturn(newSeries);

        String resultString = xtermsService.changePostsSeries(1L, oldSeriesName, newSeriesName);
        String expectedString = "✅ [시리즈 " + oldSeriesName + "] 에 소속된 모든 게시글을 " + "[시리즈 " + newSeriesName + "] 으로 변경했습니다.";

        assertThat(resultString).isEqualTo(expectedString);
        verify(userService).findUserById(userId);
        verify(seriesService).getSeries(oldSeriesName, sessionUser);

    }
}
