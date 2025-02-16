package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.domain.user.UserDesc;
import com.github.jkky_98.noteJ.file.FileStore;
import com.github.jkky_98.noteJ.web.controller.dto.SettingForm;
import com.github.jkky_98.noteJ.web.controller.form.UserSettingsForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettingService{

    private final UserService userService;
    private final FileStore fileStore;

    @Transactional
    public void saveSettings(UserSettingsForm form, Long sessionUserId) throws IOException {

        User user = userService.findUserById(sessionUserId);
        UserDesc userDesc = user.getUserDesc();

        // ProfilePic 저장 FileStore 이용
        String newProfilePicPath = profilePicPathProvider(form);

        //엔티티 update
        userDesc.updateSetting(form, newProfilePicPath);
    }

    @Transactional(readOnly = true)
    public SettingForm getUserSettingData(Long sessionUserId) {

        User user = userService.findUserById(sessionUserId);
        UserDesc userDesc = user.getUserDesc();

        return SettingForm.ofFromUserDesc(userDesc);
    }

    private String profilePicPathProvider(UserSettingsForm form) throws IOException {
        String newProfilePicPath;
        if (form.getProfilePic().isEmpty() || form.getProfilePic() == null) {
            newProfilePicPath = "default/default-profile.png";
        } else {
            newProfilePicPath = fileStore.storeFile(form.getProfilePic());
        }
        return newProfilePicPath;
    }
}
