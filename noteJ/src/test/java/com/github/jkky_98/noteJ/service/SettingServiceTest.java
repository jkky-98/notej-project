package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.user.ThemeMode;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.domain.user.UserDesc;
import com.github.jkky_98.noteJ.file.FileStore;
import com.github.jkky_98.noteJ.web.controller.dto.SettingForm;
import com.github.jkky_98.noteJ.web.controller.form.UserSettingsForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("[SettingService] Unit Tests")
class SettingServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private FileStore fileStore;

    @InjectMocks
    private SettingService settingService;

    private User user;
    private UserDesc userDesc;
    private UserSettingsForm settingsForm;

    @BeforeEach
    void setUp() {
        userDesc = spy(UserDesc.builder()
                .blogTitle("Old Title")
                .description("Old Description")
                .theme(ThemeMode.LIGHT)
                .socialEmail("old@example.com")
                .socialGitHub("oldGithub")
                .socialTwitter("oldTwitter")
                .socialFacebook("oldFacebook")
                .socialOther("oldOther")
                .commentAlarm(true)
                .noteJAlarm(false)
                .build());

        user = spy(User.builder()
                .id(1L)
                .username("testUser")
                .userDesc(userDesc)
                .build());

        settingsForm = new UserSettingsForm();
        settingsForm.setBlogTitle("New Title");
        settingsForm.setDescription("New Description");
        settingsForm.setSocialEmail("new@example.com");
        settingsForm.setSocialGitHub("newGithub");
        settingsForm.setSocialTwitter("newTwitter");
        settingsForm.setSocialFacebook("newFacebook");
        settingsForm.setSocialOther("newOther");
        settingsForm.setCommentAlarm(false);
        settingsForm.setNoteJAlarm(true);
        settingsForm.setProfilePic(new MockMultipartFile("profilePic", new byte[0])); // 빈 파일
    }

    @Test
    @DisplayName("saveSettings() - 사용자 설정 저장 (프로필 사진 없음)")
    void testSaveSettings_NoProfilePic() throws IOException {
        // given
        when(userService.findUserById(user.getId())).thenReturn(user);

        // when
        settingService.saveSettings(settingsForm, user.getId());

        // then
        verify(userService, times(1)).findUserById(user.getId());
        verify(userDesc, times(1)).updateSetting(settingsForm, "default/default-profile.png");

        assertThat(userDesc.getBlogTitle()).isEqualTo("New Title");
        assertThat(userDesc.getDescription()).isEqualTo("New Description");
        assertThat(userDesc.getTheme()).isEqualTo(ThemeMode.LIGHT);
        assertThat(userDesc.getSocialEmail()).isEqualTo("new@example.com");
        assertThat(userDesc.getSocialGitHub()).isEqualTo("newGithub");
        assertThat(userDesc.getSocialTwitter()).isEqualTo("newTwitter");
        assertThat(userDesc.getSocialFacebook()).isEqualTo("newFacebook");
        assertThat(userDesc.getSocialOther()).isEqualTo("newOther");
        assertThat(userDesc.getProfilePic()).isEqualTo("default/default-profile.png");
        assertThat(userDesc.isCommentAlarm()).isFalse();
        assertThat(userDesc.isNoteJAlarm()).isTrue();
    }

    @Test
    @DisplayName("saveSettings() - 사용자 설정 저장 (프로필 사진 포함)")
    void testSaveSettings_WithProfilePic() throws IOException {
        // given
        MockMultipartFile profilePic = new MockMultipartFile("profilePic", "image.jpg", "image/jpeg", new byte[10]);
        settingsForm.setProfilePic(profilePic);

        when(userService.findUserById(user.getId())).thenReturn(user);
        when(fileStore.storeFile(profilePic)).thenReturn("uploads/profile/image.jpg");

        // when
        settingService.saveSettings(settingsForm, user.getId());

        // then
        verify(userService, times(1)).findUserById(user.getId());
        verify(fileStore, times(1)).storeFile(profilePic);
        verify(userDesc, times(1)).updateSetting(settingsForm, "uploads/profile/image.jpg");

        assertThat(userDesc.getBlogTitle()).isEqualTo("New Title");
        assertThat(userDesc.getDescription()).isEqualTo("New Description");
        assertThat(userDesc.getTheme()).isEqualTo(ThemeMode.LIGHT);
        assertThat(userDesc.getSocialEmail()).isEqualTo("new@example.com");
        assertThat(userDesc.getSocialGitHub()).isEqualTo("newGithub");
        assertThat(userDesc.getSocialTwitter()).isEqualTo("newTwitter");
        assertThat(userDesc.getSocialFacebook()).isEqualTo("newFacebook");
        assertThat(userDesc.getSocialOther()).isEqualTo("newOther");
        assertThat(userDesc.getProfilePic()).isNotEqualTo("default/default-profile.png");
        assertThat(userDesc.isCommentAlarm()).isFalse();
        assertThat(userDesc.isNoteJAlarm()).isTrue();
    }

    @Test
    @DisplayName("getUserSettingData() - 사용자 설정 데이터 조회")
    void testGetUserSettingData() {
        // given
        when(userService.findUserById(user.getId())).thenReturn(user);
        when(user.getUserDesc()).thenReturn(userDesc);

        // when
        SettingForm result = settingService.getUserSettingData(user.getId());

        // then
        assertThat(result).isNotNull();
        assertThat(result.getBlogTitle()).isEqualTo("Old Title");
        assertThat(result.getDescription()).isEqualTo("Old Description");
        assertThat(result.getTheme()).isEqualTo(ThemeMode.LIGHT);
        assertThat(result.getSocialEmail()).isEqualTo("old@example.com");
        assertThat(result.getSocialGitHub()).isEqualTo("oldGithub");
        assertThat(result.getSocialTwitter()).isEqualTo("oldTwitter");
        assertThat(result.getSocialFacebook()).isEqualTo("oldFacebook");
        assertThat(result.getSocialOther()).isEqualTo("oldOther");
        assertThat(result.isCommentAlarm()).isTrue();
        assertThat(result.isNoteJAlarm()).isFalse();

        verify(userService, times(1)).findUserById(user.getId());
    }
}
