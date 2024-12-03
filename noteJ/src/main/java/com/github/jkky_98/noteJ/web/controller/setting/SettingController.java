package com.github.jkky_98.noteJ.web.controller.setting;

import com.github.jkky_98.noteJ.service.SettingService;
import com.github.jkky_98.noteJ.web.controller.dto.SettingDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class SettingController {

    private final SettingService settingService;

    @GetMapping("/settings")
    public String setting(Model model, HttpSession session) {
        SettingDto form = settingService.getUserSettingData(session);
        model.addAttribute("settingForm", form);
        return "setting/settings";
    }


}
