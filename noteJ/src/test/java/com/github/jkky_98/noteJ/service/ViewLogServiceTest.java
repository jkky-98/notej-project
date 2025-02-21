package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.Post;
import com.github.jkky_98.noteJ.domain.ViewLog;
import com.github.jkky_98.noteJ.repository.PostRepository;
import com.github.jkky_98.noteJ.repository.ViewLogRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.OptimisticLockException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("[ViewLogService] Unit Tests")
class ViewLogServiceTest {

    @Mock
    private ViewLogRepository viewLogRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private PostService postService;

    @InjectMocks
    private ViewLogService viewLogService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private Post post;
    private static final String CLIENT_IDENTIFIER = "IP_192.168.0.1_UA_TestBrowser";

    @BeforeEach
    void setUp() {
        post = Post.builder()
                .id(1L)
                .viewCount(10)
                .build();
    }

    @Test
    @DisplayName("increaseViewCount() - 조회수 증가 (최초 조회)")
    void testIncreaseViewCount_FirstTime() {
        // given
        when(request.getCookies()).thenReturn(null); // 쿠키 없음
        when(request.getRemoteAddr()).thenReturn("192.168.0.1");
        when(request.getHeader("User-Agent")).thenReturn("TestBrowser");
        when(viewLogRepository.existsRecentView(eq(post.getId()), eq(CLIENT_IDENTIFIER), any(LocalDateTime.class)))
                .thenReturn(false);

        // when
        viewLogService.increaseViewCount(post, request, response);

        // then
        verify(viewLogRepository, times(1)).existsRecentView(eq(post.getId()), eq(CLIENT_IDENTIFIER), any(LocalDateTime.class));
        verify(viewLogRepository, times(1)).save(any(ViewLog.class));
        assertThat(post.getViewCount()).isEqualTo(11);
    }

    @Test
    @DisplayName("increaseViewCount() - 조회수 증가 (쿠키 존재, 중복 조회 방지)")
    void testIncreaseViewCount_CookieExists() {
        // given
        Cookie[] cookies = {new Cookie("viewed_posts", "[1]")}; // 이미 조회한 기록 존재
        when(request.getCookies()).thenReturn(cookies);

        // when
        viewLogService.increaseViewCount(post, request, response);

        // then
        verify(viewLogRepository, never()).existsRecentView(anyLong(), anyString(), any(LocalDateTime.class));
        verify(viewLogRepository, never()).save(any(ViewLog.class));
    }

    @Test
    @DisplayName("increaseViewCount() - 악성 조회 감지")
    void testIncreaseViewCount_AbusiveViewer() {
        // given
        doReturn(6L).when(viewLogRepository).countRecentViews(anyString(), any(LocalDateTime.class));

        // when & then
        assertThatThrownBy(() -> viewLogService.increaseViewCount(post, request, response))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("조회 제한");

        verify(viewLogRepository, never()).save(any(ViewLog.class));
    }

    @Test
    @DisplayName("increaseViewCount() - OptimisticLockException 발생 시 재시도")
    void testIncreaseViewCount_OptimisticLockException() {
        // given
        when(request.getCookies()).thenReturn(null);
        when(postService.findById(1L)).thenReturn(
                Post.builder()
                        .id(1L)
                        .viewCount(post.getViewCount() - 1)
                        .build()
        );
        doReturn(false).when(viewLogRepository).existsRecentView(
                anyLong(),
                anyString(),
                any(LocalDateTime.class)
        );

        doThrow(new OptimisticLockException("Lock Exception"))
                .doNothing()
                .when(entityManager)
                .flush();  // ✅ flush()에서 예외 발생하도록 변경


        // when
        viewLogService.increaseViewCount(post, request, response);

        // then
        verify(entityManager, times(2)).flush(); // ✅ 첫 번째 실패 후, 두 번째 성공했는지 검증
        assertThat(post.getViewCount()).isEqualTo(11); // ✅ 조회수 증가 확인
    }

}
