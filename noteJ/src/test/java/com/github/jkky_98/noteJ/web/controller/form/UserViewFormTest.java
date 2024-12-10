package com.github.jkky_98.noteJ.web.controller.form;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserViewFormTest {

    @Test
    @DisplayName("[UserViewForm] 객체 생성 및 기본 상태 테스트")
    void userViewFormDefaultStateTest() {
        // given
        UserViewForm userViewForm = new UserViewForm();

        // then
        assertThat(userViewForm).isNotNull();
        assertThat(userViewForm.getUsername()).isNull();
        assertThat(userViewForm.getEmail()).isNull();
        assertThat(userViewForm.getUserDesc()).isNull();
        assertThat(userViewForm.getProfilePic()).isNull();
        assertThat(userViewForm.getBlogTitle()).isNull();
    }

    @Test
    @DisplayName("[UserViewForm] 필드 값 설정 및 확인 테스트")
    void userViewFormFieldAssignmentTest() {
        // given
        String username = "testuser";
        String email = "test@example.com";
        String userDesc = "This is a test description.";
        String profilePic = "http://example.com/profile-pic.jpg";
        String blogTitle = "My Blog";

        UserViewForm userViewForm = new UserViewForm();

        // when
        userViewForm.setUsername(username);
        userViewForm.setEmail(email);
        userViewForm.setUserDesc(userDesc);
        userViewForm.setProfilePic(profilePic);
        userViewForm.setBlogTitle(blogTitle);

        // then
        assertThat(userViewForm.getUsername()).isEqualTo(username);
        assertThat(userViewForm.getEmail()).isEqualTo(email);
        assertThat(userViewForm.getUserDesc()).isEqualTo(userDesc);
        assertThat(userViewForm.getProfilePic()).isEqualTo(profilePic);
        assertThat(userViewForm.getBlogTitle()).isEqualTo(blogTitle);
    }

    @Test
    @DisplayName("[UserViewForm] equals 및 hashCode 테스트")
    void userViewFormEqualsAndHashCodeTest() {
        // given
        UserViewForm userViewForm1 = new UserViewForm();
        userViewForm1.setUsername("testuser");
        userViewForm1.setEmail("test@example.com");
        userViewForm1.setUserDesc("This is a test description.");
        userViewForm1.setProfilePic("http://example.com/profile-pic.jpg");
        userViewForm1.setBlogTitle("My Blog");

        UserViewForm userViewForm2 = new UserViewForm();
        userViewForm2.setUsername("testuser");
        userViewForm2.setEmail("test@example.com");
        userViewForm2.setUserDesc("This is a test description.");
        userViewForm2.setProfilePic("http://example.com/profile-pic.jpg");
        userViewForm2.setBlogTitle("My Blog");

        // then
        assertThat(userViewForm1).isEqualTo(userViewForm2);
        assertThat(userViewForm1.hashCode()).isEqualTo(userViewForm2.hashCode());
    }
}
