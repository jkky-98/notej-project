package com.github.jkky_98.noteJ.web.controller.setting;

import com.github.jkky_98.noteJ.file.FileStore;
import com.github.jkky_98.noteJ.service.SettingService;
import com.github.jkky_98.noteJ.web.controller.dto.SettingDto;
import com.github.jkky_98.noteJ.web.controller.form.UserSettingsForm;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.MalformedURLException;

@Controller
@RequiredArgsConstructor
public class SettingController {

    private final SettingService settingService;
    private final FileStore fileStore;

    @GetMapping("/settings")
    public String settings(Model model, HttpSession session) {
        SettingDto dto = settingService.getUserSettingData(session);
        model.addAttribute("settingDto", dto);
        return "setting/settings";
    }

    @PostMapping("/settings")
    public String saveSettings(@ModelAttribute UserSettingsForm form, HttpSession session) throws IOException {
        settingService.saveSettings(form, session);
        return "redirect:/";
    }

    @ResponseBody
    @GetMapping("/profileimages/{filename}")
    public Resource downloadImage(@PathVariable String filename) throws MalformedURLException {
        return new UrlResource("file:" + fileStore.getFullPath(filename));
    }

}
