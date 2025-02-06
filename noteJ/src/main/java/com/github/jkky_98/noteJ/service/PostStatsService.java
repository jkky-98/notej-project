package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.PostHits;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.repository.PostHitsRepository;
import com.github.jkky_98.noteJ.repository.UserRepository;
import com.github.jkky_98.noteJ.web.controller.dto.PostStatsForm;
import jakarta.persistence.EntityNotFoundException;
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
    private final UserService userService;

    public PostStatsForm getPostStats(String postUrl, User sessionUser) {
        // 세션 사용자 확인
        User user = userService.findUserById(sessionUser.getId());
        // toDo: 조회수 로직 수정 필요
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
        PostStatsForm postStatsForm = new PostStatsForm();
        postStatsForm.setTotalViews(totalHits);
        postStatsForm.setTodayViews(todayHits);
        postStatsForm.setYesterdayViews(yesterdayHits);

        return postStatsForm;
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
