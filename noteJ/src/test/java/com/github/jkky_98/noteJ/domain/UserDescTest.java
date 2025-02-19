package com.github.jkky_98.noteJ.domain;

import com.github.jkky_98.noteJ.domain.user.ThemeMode;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.domain.user.UserDesc;
import com.github.jkky_98.noteJ.domain.user.UserRole;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UserDescTest {
    @Test
    @DisplayName("[UserDesc] 빌더를 통한 객체 생성 테스트")
    void userDescBuilderTest() {
        // given
        UserDesc userDesc = createUserDesc();

        // then
        assertThat(userDesc.getDescription()).isEqualTo("User description");
        assertThat(userDesc.getProfilePic()).isEqualTo("profile.jpg");
        assertThat(userDesc.getBlogTitle()).isEqualTo("My Blog");
        assertThat(userDesc.getTheme()).isEqualTo(ThemeMode.DARK);
        assertThat(userDesc.getSocialEmail()).isEqualTo("test@example.com");
        assertThat(userDesc.isCommentAlarm()).isTrue();
        assertThat(userDesc.isNoteJAlarm()).isFalse();
    }

    @Test
    @DisplayName("[UserDesc] 기본 상태 테스트")
    void userDescDefaultStateTest() {
        // given: UserDesc 객체 생성
        UserDesc userDesc = createUserDesc();

        // then: 각 필드가 올바르게 설정되었는지 검증
        assertThat(userDesc.getDescription()).isEqualTo("User description");
        assertThat(userDesc.getProfilePic()).isEqualTo("profile.jpg");
        assertThat(userDesc.getBlogTitle()).isEqualTo("My Blog");
        assertThat(userDesc.getTheme()).isEqualTo(ThemeMode.DARK);
        assertThat(userDesc.getSocialEmail()).isEqualTo("test@example.com");
        assertThat(userDesc.getSocialGitHub()).isEqualTo("github.com/test");
        assertThat(userDesc.getSocialTwitter()).isEqualTo("twitter.com/test");
        assertThat(userDesc.getSocialFacebook()).isEqualTo("facebook.com/test");
        assertThat(userDesc.getSocialOther()).isEqualTo("other.com/test");
        assertThat(userDesc.isCommentAlarm()).isTrue();
        assertThat(userDesc.isNoteJAlarm()).isFalse();
    }

    @Test
    @DisplayName("[UserDesc] updateDescription 메서드 테스트")
    void updateDescriptionTest() {
        UserDesc userDesc = createUserDesc();

        userDesc.updateDescription("updated description");
        assertThat(userDesc.getDescription()).isEqualTo("updated description");
    }

    @Test
    @DisplayName("[UserDesc] updateProfilePic 메서드 테스트")
    void updateProfilePicTest() {
        UserDesc userDesc = createUserDesc();

        userDesc.updateProfilePic("updateProfilePic");
        assertThat(userDesc.getProfilePic()).isEqualTo("updateProfilePic");
    }

    private UserDesc createUserDesc() {
        return UserDesc.builder()
                .description("User description")
                .profilePic("profile.jpg")
                .blogTitle("My Blog")
                .theme(ThemeMode.DARK)
                .socialEmail("test@example.com")
                .socialGitHub("github.com/test")
                .socialTwitter("twitter.com/test")
                .socialFacebook("facebook.com/test")
                .socialOther("other.com/test")
                .commentAlarm(true)
                .noteJAlarm(false)
                .build();
    }


}
