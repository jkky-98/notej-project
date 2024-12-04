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
        UserDesc userDesc = UserDesc.builder()
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
        // given
        UserDesc userDesc = UserDesc.builder()
                .description("Default test")
                .build();

        // then
        assertThat(userDesc.getProfilePic()).isNull();
        assertThat(userDesc.getTheme()).isNull();
        assertThat(userDesc.getSocialEmail()).isNull();
        assertThat(userDesc.isCommentAlarm()).isFalse();
        assertThat(userDesc.isNoteJAlarm()).isFalse();
    }


}
