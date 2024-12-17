package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.Post;
import com.github.jkky_98.noteJ.domain.PostHits;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.repository.PostHitsRepository;
import com.github.jkky_98.noteJ.repository.UserRepository;
import com.github.jkky_98.noteJ.web.controller.dto.PostStatsDto;
import com.github.jkky_98.noteJ.web.session.SessionUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostStatsService {

    private final PostHitsRepository postHitsRepository;
    private final UserRepository userRepository;

    public PostStatsDto getPostStats(String postUrl, User sessionUser) {
        // 세션 사용자 확인
        User user = userRepository.findById(sessionUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("User not Found"));

        // 조회수 가져오기
        List<PostHits> allPostHits = postHitsRepository.findAllPostHitsByPostId(postUrl);
        System.out.println("fdasfasdffdsa " + allPostHits);

        LocalDate today = LocalDate.now();

        // 전체 조회수: 세션 사용자가 아닌 사용자
        long totalHits = allPostHits.stream()
                .filter(postHit -> isNotSessionUser(postHit, user))
                .count();

        System.out.println("fasdfas" + totalHits);

        // 오늘 조회수
        long todayHits = allPostHits.stream()
                .filter(postHit -> isNotSessionUser(postHit, user))
                .filter(postHit -> isSameDay(postHit.getViewedAt(), today))
                .count();

        // 어제 조회수
        long yesterdayHits = allPostHits.stream()
                .filter(postHit -> isNotSessionUser(postHit, user))
                .filter(postHit -> isYesterday(postHit.getViewedAt()))
                .count();

        // DTO 반환
        PostStatsDto postStatsDto = new PostStatsDto();
        postStatsDto.setTotalViews(totalHits);
        postStatsDto.setTodayViews(todayHits);
        postStatsDto.setYesterdayViews(yesterdayHits);

        return postStatsDto;
    }

    // 세션 사용자와 다른 사용자인지 확인
    private boolean isNotSessionUser(PostHits postHit, User user) {
        return postHit.getUser() == null || !postHit.getUser().getId().equals(user.getId());
    }

    // 날짜가 같은지 확인
    private boolean isSameDay(LocalDateTime viewedAt, LocalDate targetDate) {
        return viewedAt != null && viewedAt.toLocalDate().isEqual(targetDate);
    }

    // 어제 날짜인지 확인
    private boolean isYesterday(LocalDateTime viewedAt) {
        return viewedAt != null && viewedAt.toLocalDate().isEqual(LocalDate.now().minusDays(1));
    }

}
