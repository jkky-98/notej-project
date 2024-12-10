package com.github.jkky_98.noteJ.web.controller.dto;

import com.github.jkky_98.noteJ.domain.user.ThemeMode;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SettingDtoTest {

    @Test
    @DisplayName("[SettingDto] 객체 생성 및 기본 상태 테스트")
    void settingDtoCreationAndDefaultStateTest() {
        // given
        SettingDto settingDto = new SettingDto();

        // then
        assertThat(settingDto).isNotNull();
        assertThat(settingDto.getDescription()).isNull();
        assertThat(settingDto.getProfilePic()).isNull();
        assertThat(settingDto.getBlogTitle()).isNull();
        assertThat(settingDto.getTheme()).isNull();
        assertThat(settingDto.getSocialEmail()).isNull();
        assertThat(settingDto.getSocialGitHub()).isNull();
        assertThat(settingDto.getSocialTwitter()).isNull();
        assertThat(settingDto.getSocialFacebook()).isNull();
        assertThat(settingDto.getSocialOther()).isNull();
        assertThat(settingDto.isCommentAlarm()).isFalse();
        assertThat(settingDto.isNoteJAlarm()).isFalse();
    }

    @Test
    @DisplayName("[SettingDto] 객체 값 설정 및 확인 테스트")
    void settingDtoValueAssignmentTest() {
        // given
        SettingDto settingDto = new SettingDto();
        settingDto.setDescription("Test Description");
        settingDto.setProfilePic("http://example.com/profile-pic.jpg");
        settingDto.setBlogTitle("My Blog");
        settingDto.setTheme(ThemeMode.DARK);
        settingDto.setSocialEmail("test@example.com");
        settingDto.setSocialGitHub("github.com/test");
        settingDto.setSocialTwitter("twitter.com/test");
        settingDto.setSocialFacebook("facebook.com/test");
        settingDto.setSocialOther("example.com/other");
        settingDto.setCommentAlarm(true);
        settingDto.setNoteJAlarm(true);

        // then
        assertThat(settingDto.getDescription()).isEqualTo("Test Description");
        assertThat(settingDto.getProfilePic()).isEqualTo("http://example.com/profile-pic.jpg");
        assertThat(settingDto.getBlogTitle()).isEqualTo("My Blog");
        assertThat(settingDto.getTheme()).isEqualTo(ThemeMode.DARK);
        assertThat(settingDto.getSocialEmail()).isEqualTo("test@example.com");
        assertThat(settingDto.getSocialGitHub()).isEqualTo("github.com/test");
        assertThat(settingDto.getSocialTwitter()).isEqualTo("twitter.com/test");
        assertThat(settingDto.getSocialFacebook()).isEqualTo("facebook.com/test");
        assertThat(settingDto.getSocialOther()).isEqualTo("example.com/other");
        assertThat(settingDto.isCommentAlarm()).isTrue();
        assertThat(settingDto.isNoteJAlarm()).isTrue();
    }

    @Test
    @DisplayName("[SettingDto] equals 및 hashCode 테스트")
    void settingDtoEqualsAndHashCodeTest() {
        // given
        SettingDto settingDto1 = new SettingDto();
        settingDto1.setDescription("Test Description");
        settingDto1.setProfilePic("http://example.com/profile-pic.jpg");
        settingDto1.setBlogTitle("My Blog");
        settingDto1.setTheme(ThemeMode.DARK);
        settingDto1.setSocialEmail("test@example.com");
        settingDto1.setSocialGitHub("github.com/test");
        settingDto1.setSocialTwitter("twitter.com/test");
        settingDto1.setSocialFacebook("facebook.com/test");
        settingDto1.setSocialOther("example.com/other");
        settingDto1.setCommentAlarm(true);
        settingDto1.setNoteJAlarm(true);

        SettingDto settingDto2 = new SettingDto();
        settingDto2.setDescription("Test Description");
        settingDto2.setProfilePic("http://example.com/profile-pic.jpg");
        settingDto2.setBlogTitle("My Blog");
        settingDto2.setTheme(ThemeMode.DARK);
        settingDto2.setSocialEmail("test@example.com");
        settingDto2.setSocialGitHub("github.com/test");
        settingDto2.setSocialTwitter("twitter.com/test");
        settingDto2.setSocialFacebook("facebook.com/test");
        settingDto2.setSocialOther("example.com/other");
        settingDto2.setCommentAlarm(true);
        settingDto2.setNoteJAlarm(true);

        // then
        assertThat(settingDto1).isEqualTo(settingDto2);
        assertThat(settingDto1.hashCode()).isEqualTo(settingDto2.hashCode());
    }
}
