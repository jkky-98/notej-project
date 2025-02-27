package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.Post;
import jakarta.persistence.EntityManager;
import jakarta.persistence.OptimisticLockException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class ViewLogService {

    private final EntityManager entityManager;
    private final PostService postService;
    private final CacheManager cacheManager;

    private static final String COOKIE_NAME = "viewed_posts";
    private static final int COOKIE_EXPIRATION = 60 * 60 * 24; // 24시간 (초 단위)

    @Transactional
    public void increaseViewCountV2(Long postId, HttpServletRequest request, HttpServletResponse response) {
        // 1️⃣ 쿠키 파싱: postId와 조회 시점을 Map으로 가져옴
        Map<Long, LocalDateTime> viewedPostsMap = getViewedPostsCookieEntries(request);
        String clientIdentifier = getClientIdentifier(request);
        String cacheKey = clientIdentifier + "-" + postId;

        // 2️⃣ 쿠키에 해당 postId가 있고, 조회 시점이 6시간 이내라면 조회수 증가 스킵
        if (viewedPostsMap.containsKey(postId)) {
            LocalDateTime lastViewTime = viewedPostsMap.get(postId);
            long hoursSinceLastView = Duration.between(lastViewTime, LocalDateTime.now()).toHours();
            if (hoursSinceLastView < 6) {
                log.info("⏳ 조회수 증가 스킵 - 최근 {}시간 이내에 조회됨 (postId: {})", hoursSinceLastView, postId);
                return;
            }
        }

        // 3️⃣ 쿠키에 기록이 없거나, 6시간이 지난 경우 캐시 확인 (캐시에는 key:String, value:true 형식으로 저장)
        Cache cache = cacheManager.getCache("viewLogCache");
        if (cache != null && cache.get(cacheKey) == null) {
            boolean success = false;
            int retryCount = 0;
            int maxRetry = 3;
            Post post = postService.findById(postId);

            while (!success && retryCount < maxRetry) {
                try {
                    // 4️⃣ 게시글의 조회수 증가
                    post.increaseViewCount();
                    entityManager.flush();
                    entityManager.clear();
                    log.info("✅ 조회수 증가 성공 - postId: {}, 새로운 viewCount: {}", postId, post.getViewCount());

                    // 5️⃣ 캐시에 새로운 조회 기록 저장
                    cache.put(cacheKey, true);
                    log.info("📥 캐시에 새로운 조회 기록 저장 - cacheKey: {}", cacheKey);

                    // 6️⃣ 쿠키 업데이트: 현재 시간으로 해당 postId의 조회 기록을 갱신
                    viewedPostsMap.put(postId, LocalDateTime.now());
                    String newCookieValue = buildViewedPostsCookieValue(viewedPostsMap);
                    setViewedPostsCookie(response, newCookieValue);
                    log.info("🍪 쿠키 업데이트 완료 - postId: {}, cookieValue: {}", postId, newCookieValue);

                    success = true;
                } catch (OptimisticLockException e) {
                    retryCount++;
                    log.warn("🔄 OptimisticLockException 발생 - 재시도 {}/{} (postId: {})", retryCount, maxRetry, postId);
                    if (retryCount >= maxRetry) {
                        log.error("❌ 조회수 업데이트 실패 (최대 재시도 초과) - postId: {}", postId);
                        throw new RuntimeException("조회수 업데이트 실패 (최대 재시도 초과)", e);
                    }
                    // 최신 데이터를 위해 영속성 컨텍스트 초기화 후 재조회
                    entityManager.clear();
                    post = postService.findById(postId);
                }
            }
            return;
        }

        // 7️⃣ 캐시에 이미 기록이 있는 경우 (중복 조회로 간주)
        log.info("⏳ 조회수 증가 스킵 - 캐시에 중복 조회 기록 존재 (cacheKey: {})", cacheKey);
    }

    private Map<Long, LocalDateTime> getViewedPostsCookieEntries(HttpServletRequest request) {
        String cookieValue = Optional.ofNullable(request.getCookies())
                .flatMap(cookies -> Arrays.stream(cookies)
                        .filter(cookie -> COOKIE_NAME.equals(cookie.getName()))
                        .map(Cookie::getValue)
                        .findFirst())
                .orElse("");

        log.info("🍪 현재 쿠키 값 조회 - {}", cookieValue);

        Map<Long, LocalDateTime> viewedPostsMap = new HashMap<>();
        // 정규표현식: [postId:timestamp]
        Pattern pattern = Pattern.compile("\\[(\\d+):(\\d+)]");
        Matcher matcher = pattern.matcher(cookieValue);
        while (matcher.find()) {
            try {
                Long id = Long.parseLong(matcher.group(1));
                Long timestamp = Long.parseLong(matcher.group(2));
                LocalDateTime viewTime = Instant.ofEpochMilli(timestamp)
                        .atZone(ZoneId.systemDefault()).toLocalDateTime();
                viewedPostsMap.put(id, viewTime);
            } catch (NumberFormatException e) {
                log.error("숫자 파싱 실패: {} 또는 {}", matcher.group(1), matcher.group(2), e);
            }
        }
        return viewedPostsMap;
    }

    private String buildViewedPostsCookieValue(Map<Long, LocalDateTime> viewedPostsMap) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Long, LocalDateTime> entry : viewedPostsMap.entrySet()) {
            long postId = entry.getKey();
            // timestamp를 밀리초(epochMillis)로 변환
            long timestamp = entry.getValue().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            sb.append("[").append(postId).append(":").append(timestamp).append("]");
        }
        return sb.toString();
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