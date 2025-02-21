package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.Post;
import com.github.jkky_98.noteJ.domain.ViewLog;
import com.github.jkky_98.noteJ.repository.PostRepository;
import com.github.jkky_98.noteJ.repository.ViewLogRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
@Service
@RequiredArgsConstructor
@Slf4j
public class ViewLogService {

    private final ViewLogRepository viewLogRepository;
    private final EntityManager entityManager;
    private final PostService postService;

    private static final String COOKIE_NAME = "viewed_posts";
    private static final int COOKIE_EXPIRATION = 60 * 60 * 24; // 24시간 (초 단위)
    private static final int MAX_VIEWS_PER_HOUR = 5; // 1시간 내 최대 조회 가능 횟수

    /**
     * 조회수 증가 (쿠키 기반 중복 확인 + 악성 이용 방지)
     */
    @Transactional
    public void increaseViewCount(Post post, HttpServletRequest request, HttpServletResponse response) {
        String viewedPosts = getViewedPostsCookie(request);
        String clientIdentifier = getClientIdentifier(request); // IP + User-Agent 기반 식별자

        Long postId = post.getId();

        log.info("🔍 조회 요청 - postId: {}, clientIdentifier: {}", postId, clientIdentifier);

        // 1️⃣ 악성 이용 방지: 같은 사용자가 너무 많은 조회를 시도하는 경우 차단
        if (isAbusiveViewer(clientIdentifier)) {
            log.warn("🚨 조회 제한 - 악성 조회 감지! clientIdentifier: {}", clientIdentifier);
            throw new RuntimeException("조회 제한: 너무 많은 조회 요청이 감지되었습니다.");
        }

        // 2️⃣ 쿠키에서 해당 게시글 ID가 포함되어 있으면 중복 조회 방지 (DB 조회 없이 처리)
        if (!viewedPosts.isEmpty() && viewedPosts.contains("[" + postId + "]")) {
            log.info("⏳ 조회수 증가 스킵 - 쿠키에 이미 존재 (postId: {}, clientIdentifier: {})", postId, clientIdentifier);
            return;
        }

        // 3️⃣ 쿠키가 없는 경우에만 `ViewLog`를 조회하여 중복 조회 방지
        if (viewedPosts.isEmpty()) {
            boolean alreadyViewed = viewLogRepository.existsRecentView(
                    postId,
                    clientIdentifier,
                    LocalDateTime.now().minusHours(24)
            );

            if (alreadyViewed) {
                log.info("⏳ 조회수 증가 스킵 - DB에서 중복 조회 확인됨 (postId: {}, clientIdentifier: {})", postId, clientIdentifier);
                return;
            }
        }

        boolean success = false;
        int retryCount = 0;
        int maxRetry = 3;

        while (!success && retryCount < maxRetry) {
            try {
                post.increaseViewCount();
                entityManager.flush();
                entityManager.clear();
                log.info("✅ 조회수 증가 성공 - postId: {}, 새로운 viewCount: {}", postId, post.getViewCount());


                // 4️⃣ 새로운 조회 기록을 `ViewLog`에 저장 (쿠키가 없던 경우만 저장)
                if (viewedPosts.isEmpty()) {
                    viewLogRepository.save(
                            ViewLog.builder()
                                    .identifier(clientIdentifier)
                                    .post(post)
                                    .build()
                    );
                    log.info("📝 ViewLog 저장 완료 - postId: {}, clientIdentifier: {}", postId, clientIdentifier);
                }

                // 5️⃣ 새로운 쿠키 값 생성 (기존 값 + 새로운 조회 기록 추가)
                String newCookieValue = viewedPosts + "[" + postId + "]";
                setViewedPostsCookie(response, newCookieValue);

                log.info("🍪 새로운 쿠키 설정 - postId: {}, cookieValue: {}", postId, newCookieValue);

                success = true;

            } catch (OptimisticLockException e) {
                retryCount++;
                log.warn("🔄 OptimisticLockException 발생 - 재시도 {}/{} (postId: {})", retryCount, maxRetry, postId);
                if (retryCount >= maxRetry) {
                    log.error("❌ 조회수 업데이트 실패 (최대 재시도 초과) - postId: {}", postId);
                    throw new RuntimeException("조회수 업데이트 실패 (최대 재시도 초과)", e);
                }

                // 🔴 기존 post 엔티티를 버리고 최신 데이터를 다시 조회
                entityManager.clear();  // ✅ 기존 영속성 컨텍스트 비우기
                post = postService.findById(postId);
            }
        }
    }


    /**
     * 최근 1시간 동안 같은 사용자가 너무 많은 조회를 시도하는지 확인
     */
    private boolean isAbusiveViewer(String clientIdentifier) {
        long recentViews = viewLogRepository.countRecentViews(clientIdentifier, LocalDateTime.now().minusHours(1));
        boolean isAbusive = recentViews >= MAX_VIEWS_PER_HOUR;

        if (isAbusive) {
            log.warn("🚨 악성 조회 탐지 - clientIdentifier: {}, 최근 1시간 조회수: {}", clientIdentifier, recentViews);
        }
        return isAbusive;
    }

    /**
     * 클라이언트 식별자 생성 (IP + User-Agent 기반)
     */
    private String getClientIdentifier(HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        String identifier = "IP_" + ip + "_UA_" + userAgent;

        log.debug("🆔 클라이언트 식별자 생성 - {}", identifier);
        return identifier;
    }

    /**
     * 현재 쿠키에서 조회된 게시글 목록 가져오기
     */
    private String getViewedPostsCookie(HttpServletRequest request) {
        String cookieValue = Optional.ofNullable(request.getCookies())
                .flatMap(cookies -> Arrays.stream(cookies)
                        .filter(cookie -> COOKIE_NAME.equals(cookie.getName()))
                        .map(Cookie::getValue)
                        .findFirst())
                .orElse("");

        log.debug("🍪 현재 쿠키 값 조회 - {}", cookieValue);
        return cookieValue;
    }

    /**
     * 새로운 조회 기록을 쿠키에 저장
     */
    private void setViewedPostsCookie(HttpServletResponse response, String cookieValue) {
        Cookie cookie = new Cookie(COOKIE_NAME, cookieValue);
        cookie.setMaxAge(COOKIE_EXPIRATION); // 24시간 유지
        cookie.setPath("/"); // 모든 경로에서 유효
        response.addCookie(cookie);

        log.info("🍪 쿠키 설정 완료 - cookieValue: {}", cookieValue);
    }
}