package com.github.jkky_98.noteJ.domain;

import com.github.jkky_98.noteJ.TestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("[ViewLog] Unit Tests")
class ViewLogTest {

    @Test
    @DisplayName("[ViewLog] 생성 방식 테스트 : 빌더 패턴 적용")
    void builderPatternTest() {
        // Post 엔티티는 간단히 빌더로 생성 (실제 사용 시 필요한 필드를 설정)
        Post dummyPost = Post.builder().build();

        ViewLog viewLog = ViewLog.builder()
                .identifier("testIdentifier")
                .post(dummyPost)
                .build();

        assertThat(viewLog.getIdentifier()).isEqualTo("testIdentifier");
        assertThat(viewLog.getPost()).isEqualTo(dummyPost);
    }

    @Test
    @DisplayName("[ViewLog] 기본 상태 테스트")
    void defaultStateTest() {
        // 빌더로 아무 값도 설정하지 않았을 경우 기본 상태 검증
        ViewLog viewLog = ViewLog.builder().build();

        // id는 아직 persist되지 않았으므로 null
        assertThat(viewLog.getId()).isNull();
        // identifier, post는 명시적으로 설정하지 않았으므로 null
        assertThat(viewLog.getIdentifier()).isNull();
        assertThat(viewLog.getPost()).isNull();
    }

    @Test
    @DisplayName("[ViewLog] isExpired() - 만료된 경우 (24시간 전보다 이전)")
    void isExpiredTest_true() {
        Post dummyPost = Post.builder().build();
        // 현재 시간보다 25시간 전으로 설정하여 만료 상태로 만듦
        LocalDateTime expiredTime = LocalDateTime.now().minusHours(25);

        ViewLog viewLog = ViewLog.builder()
                .identifier("testIdentifier")
                .post(dummyPost)
                .build();
        // 리플렉션 유틸리티로 createDt 필드에 expiredTime 주입
        TestUtils.setField(viewLog, "createDt", expiredTime);

        assertThat(viewLog.isExpired()).isTrue();
    }

    @Test
    @DisplayName("[ViewLog] isExpired() - 만료되지 않은 경우 (24시간 내)")
    void isExpiredTest_false() {
        Post dummyPost = Post.builder().build();
        // 현재 시간보다 2시간 전으로 설정하여 만료되지 않은 상태로 만듦
        LocalDateTime recentTime = LocalDateTime.now().minusHours(2);

        ViewLog viewLog = ViewLog.builder()
                .identifier("testIdentifier")
                .post(dummyPost)
                .build();
        TestUtils.setField(viewLog, "createDt", recentTime);

        assertThat(viewLog.isExpired()).isFalse();
    }
}