package com.github.jkky_98.noteJ.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class SettingController {

    @GetMapping("/settings")
    public String setting() {
        return "setting/settings";
    }


}
