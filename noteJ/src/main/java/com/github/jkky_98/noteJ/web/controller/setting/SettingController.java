package com.github.jkky_98.noteJ.web.controller.setting;

import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.file.FileStore;
import com.github.jkky_98.noteJ.service.setting.SettingService;
import com.github.jkky_98.noteJ.web.controller.dto.SettingDto;
import com.github.jkky_98.noteJ.web.controller.form.UserSettingsForm;
import com.github.jkky_98.noteJ.web.session.SessionConst;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Controller
@RequiredArgsConstructor
public class SettingController {

    private final SettingService settingService;
    private final FileStore fileStore;

    @GetMapping("/settings")
    public String settings(Model model, @SessionAttribute(SessionConst.LOGIN_USER) User sessionUser) {
        SettingDto dto = settingService.getUserSettingData(sessionUser);
        model.addAttribute("settingDto", dto);
        return "setting/settings";
    }

    @PostMapping("/settings")
    public String saveSettings(@ModelAttribute UserSettingsForm form, @SessionAttribute(SessionConst.LOGIN_USER) User sessionUser) throws IOException {
        settingService.saveSettings(form, sessionUser);
        return "redirect:/";
    }
}
