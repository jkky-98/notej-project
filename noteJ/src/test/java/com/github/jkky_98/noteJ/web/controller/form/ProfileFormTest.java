package com.github.jkky_98.noteJ.web.controller.form;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ProfileFormTest {
    @Test
    @DisplayName("[ProfileForm] 객체 생성 및 기본 상태 테스트")
    void profileFormDefaultStateTest() {
        // given
        ProfileForm profileForm = new ProfileForm();
        // then
        assertThat(profileForm).isNotNull();
        assertThat(profileForm.getUsername()).isNull();
        assertThat(profileForm.getProfilePic()).isNull();
        assertThat(profileForm.getDescription()).isNull();
        assertThat(profileForm.getSocialEmail()).isNull();
        assertThat(profileForm.getSocialGitHub()).isNull();
        assertThat(profileForm.getSocialTwitter()).isNull();
        assertThat(profileForm.getSocialFacebook()).isNull();
        assertThat(profileForm.getSocialOther()).isNull();
        assertThat(profileForm.getFollowers()).isZero();
        assertThat(profileForm.getFollowings()).isZero();
    }

    @Test
    @DisplayName("[ProfileForm] 필드 값 설정 및 확인 테스트")
    void profileFormValueAssignmentTest() {
        // given
        ProfileForm profileForm = new ProfileForm();

        // when
        profileForm.setUsername("testuser");
        profileForm.setProfilePic("http://example.com/profile-pic.jpg");
        profileForm.setDescription("This is a test description");
        profileForm.setSocialEmail("test@example.com");
        profileForm.setSocialGitHub("github.com/testuser");
        profileForm.setSocialTwitter("twitter.com/testuser");
        profileForm.setSocialFacebook("facebook.com/testuser");
        profileForm.setSocialOther("other.com/testuser");
        profileForm.setFollowers(100);
        profileForm.setFollowings(200);

        // then
        assertThat(profileForm.getUsername()).isEqualTo("testuser");
        assertThat(profileForm.getProfilePic()).isEqualTo("http://example.com/profile-pic.jpg");
        assertThat(profileForm.getDescription()).isEqualTo("This is a test description");
        assertThat(profileForm.getSocialEmail()).isEqualTo("test@example.com");
        assertThat(profileForm.getSocialGitHub()).isEqualTo("github.com/testuser");
        assertThat(profileForm.getSocialTwitter()).isEqualTo("twitter.com/testuser");
        assertThat(profileForm.getSocialFacebook()).isEqualTo("facebook.com/testuser");
        assertThat(profileForm.getSocialOther()).isEqualTo("other.com/testuser");
        assertThat(profileForm.getFollowers()).isEqualTo(100);
        assertThat(profileForm.getFollowings()).isEqualTo(200);
    }

    @Test
    @DisplayName("[ProfileForm] equals 및 hashCode 테스트")
    void profileFormEqualsAndHashCodeTest() {
        // given
        ProfileForm profileForm1 = new ProfileForm();
        profileForm1.setUsername("testuser");
        profileForm1.setProfilePic("http://example.com/profile-pic.jpg");
        profileForm1.setDescription("This is a test description");
        profileForm1.setSocialEmail("test@example.com");
        profileForm1.setSocialGitHub("github.com/testuser");
        profileForm1.setSocialTwitter("twitter.com/testuser");
        profileForm1.setSocialFacebook("facebook.com/testuser");
        profileForm1.setSocialOther("other.com/testuser");
        profileForm1.setFollowers(100);
        profileForm1.setFollowings(200);

        ProfileForm profileForm2 = new ProfileForm();
        profileForm2.setUsername("testuser");
        profileForm2.setProfilePic("http://example.com/profile-pic.jpg");
        profileForm2.setDescription("This is a test description");
        profileForm2.setSocialEmail("test@example.com");
        profileForm2.setSocialGitHub("github.com/testuser");
        profileForm2.setSocialTwitter("twitter.com/testuser");
        profileForm2.setSocialFacebook("facebook.com/testuser");
        profileForm2.setSocialOther("other.com/testuser");
        profileForm2.setFollowers(100);
        profileForm2.setFollowings(200);

        // then
        assertThat(profileForm1).isEqualTo(profileForm2);
        assertThat(profileForm1.hashCode()).isEqualTo(profileForm2.hashCode());
    }
}
