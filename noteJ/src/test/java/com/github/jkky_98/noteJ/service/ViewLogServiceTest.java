package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.Post;
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
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("[ViewLogService] Unit Tests")
class ViewLogServiceTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private PostService postService;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache viewLogCache;

    @InjectMocks
    private ViewLogService viewLogService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private Post post;
    private static final Long POST_ID = 1L;
    // 클라이언트 식별자는 IP와 User-Agent를 조합하는데, 테스트에서 아래와 같이 가정한다.
    private static final String EXPECTED_CLIENT_IDENTIFIER = "IP_127.0.0.1_UA_TestAgent";

    @BeforeEach
    void setUp() {
        post = Post.builder()
                .id(POST_ID)
                .viewCount(10)
                .build();
    }

    @Test
    @DisplayName("조회수 증가 - 쿠키 없음 및 캐시 miss")
    void testIncreaseViewCount_NoCookieAndNoCache() {
        // 쿠키가 없다고 가정 (null 반환)
        when(request.getCookies()).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("User-Agent")).thenReturn("TestAgent");

        // 캐시: viewLogCache 캐시가 존재하고, 해당 cacheKey는 비어있음
        when(cacheManager.getCache("viewLogCache")).thenReturn(viewLogCache);
        when(viewLogCache.get(anyString())).thenReturn(null);

        // postService에서 조회할 post 반환
        when(postService.findById(POST_ID)).thenReturn(post);

        viewLogService.increaseViewCountV2(POST_ID, request, response);

        // 조회수 증가 후 post viewCount가 11이 되어야 함
        assertThat(post.getViewCount()).isEqualTo(11);
        // 캐시에 새로운 기록이 저장되어야 함 (cacheKey는 EXPECTED_CLIENT_IDENTIFIER + "-" + POST_ID)
        verify(viewLogCache).put(eq(EXPECTED_CLIENT_IDENTIFIER + "-" + POST_ID), eq(true));
        // response에 쿠키가 추가되었는지 확인
        verify(response).addCookie(any(Cookie.class));
        // flush와 clear가 호출되었는지 검증
        verify(entityManager, atLeastOnce()).flush();
        verify(entityManager, atLeastOnce()).clear();
    }

    @Test
    @DisplayName("조회수 증가 스킵 - 쿠키에 기록 존재(6시간 이내)")
    void testIncreaseViewCount_CookieExistsWithin6Hours() {
        // 쿠키가 존재하며, 해당 postId의 조회 시각이 현재 6시간 이내임.
        long nowMillis = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        String cookieValue = "[" + POST_ID + ":" + nowMillis + "]";
        Cookie cookie = new Cookie("viewed_posts", cookieValue);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("User-Agent")).thenReturn("TestAgent");

        // 호출 시 조회수 증가 로직이 스킵되어야 하므로, postService.findById()는 호출되지 않아야 함.
        viewLogService.increaseViewCountV2(POST_ID, request, response);

        // 조회수는 그대로여야 함
        assertThat(post.getViewCount()).isEqualTo(10);
        verify(postService, never()).findById(anyLong());
        // 캐시에도 접근하지 않아야 함.
        verify(cacheManager, never()).getCache("viewLogCache");
    }

    @Test
    @DisplayName("조회수 증가 - 쿠키 기록은 있으나 조회 시각이 6시간 초과")
    void testIncreaseViewCount_CookieExistsButOlderThan6Hours() {
        // 쿠키가 존재하지만, 조회 시각이 7시간 전인 경우 -> 조회수 증가 로직 실행
        long pastMillis = LocalDateTime.now().minusHours(7).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        String cookieValue = "[" + POST_ID + ":" + pastMillis + "]";
        Cookie cookie = new Cookie("viewed_posts", cookieValue);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("User-Agent")).thenReturn("TestAgent");

        // 캐시 설정: cache miss
        when(cacheManager.getCache("viewLogCache")).thenReturn(viewLogCache);
        when(viewLogCache.get(anyString())).thenReturn(null);
        when(postService.findById(POST_ID)).thenReturn(post);

        viewLogService.increaseViewCountV2(POST_ID, request, response);

        // 조회수 증가 확인
        assertThat(post.getViewCount()).isEqualTo(11);
        verify(viewLogCache).put(eq(EXPECTED_CLIENT_IDENTIFIER + "-" + POST_ID), eq(true));
        verify(response).addCookie(any(Cookie.class));
    }

    @Test
    @DisplayName("조회수 증가 스킵 - 캐시에 이미 조회 기록 존재")
    void testIncreaseViewCount_CacheAlreadyExists() {
        // 쿠키가 없으므로 null 반환
        when(request.getCookies()).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("User-Agent")).thenReturn("TestAgent");

        // 캐시: 이미 해당 cacheKey가 존재
        when(cacheManager.getCache("viewLogCache")).thenReturn(viewLogCache);
        when(viewLogCache.get(anyString())).thenReturn(new Cache.ValueWrapper() {
            @Override
            public Object get() {
                return true;
            }
        });

        viewLogService.increaseViewCountV2(POST_ID, request, response);

        // 조회수 증가가 스킵되므로 viewCount는 그대로여야 함
        assertThat(post.getViewCount()).isEqualTo(10);
        verify(postService, never()).findById(anyLong());
    }

    @Test
    @DisplayName("조회수 증가 - OptimisticLockException 발생 시 재시도")
    void testIncreaseViewCount_OptimisticLockExceptionRetry() {
        // 쿠키가 없으므로 null 반환
        when(request.getCookies()).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("User-Agent")).thenReturn("TestAgent");

        // 캐시: cache miss
        when(cacheManager.getCache("viewLogCache")).thenReturn(viewLogCache);
        when(viewLogCache.get(anyString())).thenReturn(null);

        // 첫 번째 호출 시에는 기존 post (viewCount=10)를, 두 번째 호출 시에는 fresh한 post 인스턴스를 반환하도록 함
        Post freshPost = Post.builder().id(POST_ID).viewCount(10).build();
        when(postService.findById(POST_ID)).thenReturn(post).thenReturn(freshPost);

        // 첫 번째 flush 호출 시 OptimisticLockException 발생, 두 번째 호출은 정상 처리
        doThrow(new OptimisticLockException("Lock Exception"))
                .doNothing()
                .when(entityManager).flush();

        viewLogService.increaseViewCountV2(POST_ID, request, response);

        // flush가 2번 호출되어 재시도 했음을 확인
        verify(entityManager, times(2)).flush();
        // 두 번째 시도에서 freshPost의 조회수 증가로 인해 최종 viewCount가 11이어야 함
        assertThat(freshPost.getViewCount()).isEqualTo(11);
        verify(viewLogCache).put(eq(EXPECTED_CLIENT_IDENTIFIER + "-" + POST_ID), eq(true));
    }
}
