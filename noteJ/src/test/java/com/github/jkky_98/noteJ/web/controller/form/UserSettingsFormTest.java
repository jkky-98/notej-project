package com.github.jkky_98.noteJ.web.controller.form;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;

class UserSettingsFormTest {

    @Test
    @DisplayName("[UserSettingsForm] 객체 생성 및 기본 상태 테스트")
    void userSettingsFormDefaultStateTest() {
        // given
        UserSettingsForm form = new UserSettingsForm();

        // then
        assertThat(form).isNotNull();
        assertThat(form.getProfilePic()).isNull();
        assertThat(form.getBlogTitle()).isNull();
        assertThat(form.getDescription()).isNull();
        assertThat(form.getTheme()).isNull();
        assertThat(form.getSocialEmail()).isNull();
        assertThat(form.getSocialGitHub()).isNull();
        assertThat(form.getSocialTwitter()).isNull();
        assertThat(form.getSocialFacebook()).isNull();
        assertThat(form.getSocialOther()).isNull();
        assertThat(form.isCommentAlarm()).isFalse();
        assertThat(form.isNoteJAlarm()).isFalse();
    }

    @Test
    @DisplayName("[UserSettingsForm] 필드 값 설정 및 확인 테스트")
    void userSettingsFormFieldAssignmentTest() {
        // given
        MockMultipartFile profilePic = new MockMultipartFile("profilePic", "profile.jpg", "image/jpeg", new byte[]{});
        String blogTitle = "My Blog";
        String description = "This is a test description.";
        String theme = "LIGHT";
        String socialEmail = "test@example.com";
        String socialGitHub = "github.com/test";
        String socialTwitter = "twitter.com/test";
        String socialFacebook = "facebook.com/test";
        String socialOther = "other.com/test";
        boolean commentAlarm = true;
        boolean noteJAlarm = true;

        UserSettingsForm form = new UserSettingsForm();

        // when
        form.setProfilePic(profilePic);
        form.setBlogTitle(blogTitle);
        form.setDescription(description);
        form.setTheme(theme);
        form.setSocialEmail(socialEmail);
        form.setSocialGitHub(socialGitHub);
        form.setSocialTwitter(socialTwitter);
        form.setSocialFacebook(socialFacebook);
        form.setSocialOther(socialOther);
        form.setCommentAlarm(commentAlarm);
        form.setNoteJAlarm(noteJAlarm);

        // then
        assertThat(form.getProfilePic()).isEqualTo(profilePic);
        assertThat(form.getBlogTitle()).isEqualTo(blogTitle);
        assertThat(form.getDescription()).isEqualTo(description);
        assertThat(form.getTheme()).isEqualTo(theme);
        assertThat(form.getSocialEmail()).isEqualTo(socialEmail);
        assertThat(form.getSocialGitHub()).isEqualTo(socialGitHub);
        assertThat(form.getSocialTwitter()).isEqualTo(socialTwitter);
        assertThat(form.getSocialFacebook()).isEqualTo(socialFacebook);
        assertThat(form.getSocialOther()).isEqualTo(socialOther);
        assertThat(form.isCommentAlarm()).isEqualTo(commentAlarm);
        assertThat(form.isNoteJAlarm()).isEqualTo(noteJAlarm);
    }

    @Test
    @DisplayName("[UserSettingsForm] equals 및 hashCode 테스트")
    void userSettingsFormEqualsAndHashCodeTest() {
        // given
        MockMultipartFile profilePic = new MockMultipartFile("profilePic", "profile.jpg", "image/jpeg", new byte[]{});
        UserSettingsForm form1 = new UserSettingsForm();
        form1.setProfilePic(profilePic);
        form1.setBlogTitle("My Blog");
        form1.setDescription("This is a test description.");
        form1.setTheme("LIGHT");
        form1.setSocialEmail("test@example.com");
        form1.setSocialGitHub("github.com/test");
        form1.setSocialTwitter("twitter.com/test");
        form1.setSocialFacebook("facebook.com/test");
        form1.setSocialOther("other.com/test");
        form1.setCommentAlarm(true);
        form1.setNoteJAlarm(true);

        UserSettingsForm form2 = new UserSettingsForm();
        form2.setProfilePic(profilePic);
        form2.setBlogTitle("My Blog");
        form2.setDescription("This is a test description.");
        form2.setTheme("LIGHT");
        form2.setSocialEmail("test@example.com");
        form2.setSocialGitHub("github.com/test");
        form2.setSocialTwitter("twitter.com/test");
        form2.setSocialFacebook("facebook.com/test");
        form2.setSocialOther("other.com/test");
        form2.setCommentAlarm(true);
        form2.setNoteJAlarm(true);

        // then
        assertThat(form1).isEqualTo(form2);
        assertThat(form1.hashCode()).isEqualTo(form2.hashCode());
    }
}
