package com.github.jkky_98.noteJ.repository.userdesc;
import com.github.jkky_98.noteJ.domain.FileMetadata;
import com.github.jkky_98.noteJ.domain.user.ThemeMode;
import com.github.jkky_98.noteJ.domain.user.UserDesc;
import com.github.jkky_98.noteJ.file.FileStoreLocal;
import com.github.jkky_98.noteJ.web.controller.form.UserSettingsForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DataJpaTest
@DisplayName("[UserDescRepository] UserDesc updateSetting 테스트")
@ActiveProfiles("local")
public class UserDescUpdateSettingTest {

    private UserDesc userDesc;
    private FileStoreLocal fileStoreLocal;

    @BeforeEach
    void setup() {
        // 초기 UserDesc 엔티티
        userDesc = UserDesc.builder()
                .description("Old Description")
                .profilePic("old-pic.png")
                .blogTitle("Old Blog Title")
                .theme(ThemeMode.LIGHT)
                .socialEmail("old@example.com")
                .socialGitHub("https://github.com/old")
                .socialTwitter("https://twitter.com/old")
                .socialFacebook("https://facebook.com/old")
                .socialOther("https://example.com/old")
                .commentAlarm(false)
                .noteJAlarm(true)
                .build();

        // Mock FileStore
        fileStoreLocal = Mockito.mock(FileStoreLocal.class);
    }

    @Test
    @DisplayName("[UserDescRepository] 프로필 사진과 설정 업데이트 테스트")
    void testUpdateSettingWithProfilePic() throws IOException {
        // Mock 파일 저장 결과
        FileMetadata mockFileMetadata = FileMetadata.builder()
                .storedFileName("new-pic.png")
                .build();

        when(fileStoreLocal.storeFile(any())).thenReturn(mockFileMetadata);

        // 준비된 입력 데이터
        UserSettingsForm form = new UserSettingsForm();
        form.setProfilePic(Mockito.mock(MultipartFile.class));
        form.setDescription("New Description");
        form.setBlogTitle("New Blog Title");
        form.setTheme("DARK");
        form.setSocialEmail("new@example.com");
        form.setSocialGitHub("https://github.com/new");
        form.setSocialTwitter("https://twitter.com/new");
        form.setSocialFacebook("https://facebook.com/new");
        form.setSocialOther("https://example.com/new");
        form.setCommentAlarm(true);
        form.setNoteJAlarm(false);

        // 메서드 실행
        userDesc.updateSetting(form, fileStoreLocal);

        // 검증
        assertThat(userDesc.getProfilePic()).isEqualTo("new-pic.png");
        assertThat(userDesc.getDescription()).isEqualTo("New Description");
        assertThat(userDesc.getBlogTitle()).isEqualTo("New Blog Title");
        assertThat(userDesc.getTheme()).isEqualTo(ThemeMode.DARK);
        assertThat(userDesc.getSocialEmail()).isEqualTo("new@example.com");
        assertThat(userDesc.getSocialGitHub()).isEqualTo("https://github.com/new");
        assertThat(userDesc.getSocialTwitter()).isEqualTo("https://twitter.com/new");
        assertThat(userDesc.getSocialFacebook()).isEqualTo("https://facebook.com/new");
        assertThat(userDesc.getSocialOther()).isEqualTo("https://example.com/new");
        assertThat(userDesc.isCommentAlarm()).isTrue();
        assertThat(userDesc.isNoteJAlarm()).isFalse();

        // Mock 메서드 호출 검증
        verify(fileStoreLocal, times(1)).storeFile(any());
    }

    @Test
    @DisplayName("[UserDescRepository] 프로필 사진 없이 설정 업데이트 테스트")
    void testUpdateSettingWithoutProfilePic() throws IOException {
        // 준비된 입력 데이터
        UserSettingsForm form = new UserSettingsForm();
        form.setProfilePic(null);  // 프로필 사진 없음
        form.setDescription("New Description Without Pic");
        form.setBlogTitle("New Blog Title Without Pic");
        form.setTheme("DARK");
        form.setSocialEmail("new-without-pic@example.com");
        form.setSocialGitHub("https://github.com/new-without-pic");
        form.setSocialTwitter("https://twitter.com/new-without-pic");
        form.setSocialFacebook("https://facebook.com/new-without-pic");
        form.setSocialOther("https://example.com/new-without-pic");
        form.setCommentAlarm(true);
        form.setNoteJAlarm(false);

        // 메서드 실행
        userDesc.updateSetting(form, fileStoreLocal);

        // 검증
        assertThat(userDesc.getProfilePic()).isEqualTo("old-pic.png");  // 기존 프로필 사진 유지
        assertThat(userDesc.getDescription()).isEqualTo("New Description Without Pic");
        assertThat(userDesc.getBlogTitle()).isEqualTo("New Blog Title Without Pic");
        assertThat(userDesc.getTheme()).isEqualTo(ThemeMode.DARK);
        assertThat(userDesc.getSocialEmail()).isEqualTo("new-without-pic@example.com");
        assertThat(userDesc.getSocialGitHub()).isEqualTo("https://github.com/new-without-pic");
        assertThat(userDesc.getSocialTwitter()).isEqualTo("https://twitter.com/new-without-pic");
        assertThat(userDesc.getSocialFacebook()).isEqualTo("https://facebook.com/new-without-pic");
        assertThat(userDesc.getSocialOther()).isEqualTo("https://example.com/new-without-pic");
        assertThat(userDesc.isCommentAlarm()).isTrue();
        assertThat(userDesc.isNoteJAlarm()).isFalse();

        // Mock 메서드 호출 검증
        verify(fileStoreLocal, times(0)).storeFile(any());  // 파일 저장 메서드 호출되지 않음
    }
}
