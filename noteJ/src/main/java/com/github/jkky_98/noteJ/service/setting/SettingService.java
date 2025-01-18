package com.github.jkky_98.noteJ.service.setting;

import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.web.controller.dto.SettingDto;
import com.github.jkky_98.noteJ.web.controller.form.UserSettingsForm;

import java.io.IOException;
import java.util.Optional;

public interface SettingService {
    Optional<String> saveSettings(UserSettingsForm form, User sessionUser) throws IOException;
    SettingDto getUserSettingData(User sessionUser);
}
