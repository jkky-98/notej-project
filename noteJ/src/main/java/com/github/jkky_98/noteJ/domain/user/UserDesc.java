package com.github.jkky_98.noteJ.domain.user;

import com.github.jkky_98.noteJ.domain.base.BaseTimeEntity;
import com.github.jkky_98.noteJ.web.controller.form.SignUpForm;
import com.github.jkky_98.noteJ.web.controller.form.UserSettingsForm;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 기본 생성자
@AllArgsConstructor(access = AccessLevel.PRIVATE) // 빌더와 함께 사용할 모든 필드 생성자
public class UserDesc extends BaseTimeEntity {
    @Id
    @GeneratedValue
    @Column(name = "user_desc_id")
    private Long id;

    private String description;

    private String profilePic;

    private String blogTitle;

    @Enumerated(EnumType.STRING)
    private ThemeMode theme;

    private String socialEmail;

    private String socialGitHub;

    private String socialTwitter;

    private String socialFacebook;

    private String socialOther;

    private boolean commentAlarm;

    private boolean noteJAlarm;

    // 연관관계
    @OneToOne(mappedBy = "userDesc")
    private User user;


    public void updateSetting(UserSettingsForm form, String newProfilePicPath) {
        // 다른 필드 업데이트
        description = form.getDescription();
        blogTitle = form.getBlogTitle();
        profilePic = newProfilePicPath;
        socialEmail = form.getSocialEmail();
        socialGitHub = form.getSocialGitHub();
        socialFacebook = form.getSocialFacebook();
        socialTwitter = form.getSocialTwitter();
        socialOther = form.getSocialOther();
        commentAlarm = form.isCommentAlarm();
        noteJAlarm = form.isNoteJAlarm();
    }


    public void updateDescription(String updatedDescription) {
        description = updatedDescription;
    }

    public void updateProfilePic(String updatedProfilePic) {
        profilePic = updatedProfilePic;
    }
}

