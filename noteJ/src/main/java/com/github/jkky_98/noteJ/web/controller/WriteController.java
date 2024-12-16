package com.github.jkky_98.noteJ.web.controller;

import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.service.WriteService;
import com.github.jkky_98.noteJ.web.controller.form.WriteForm;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.IOException;

@Controller
@RequiredArgsConstructor
public class WriteController {

    private final WriteService writeService;

    @GetMapping("/write")
    public String write(HttpSession session, @ModelAttribute WriteForm writeForm, Model model) {
        User sessionUser = (User) session.getAttribute("loginUser");
        model.addAttribute("seriesList", writeService.getWrite(sessionUser));
        return "write";
    }

    @PostMapping("/write")
    public String writeSave(@ModelAttribute WriteForm form, HttpSession session) throws IOException {
        User sessionUser = (User) session.getAttribute("loginUser");
        writeService.saveWrite(form, sessionUser, false);
        return "redirect:/";
    }

//    @PostMapping("/write/savetemp")
//    public String writeSaveTemp(@ModelAttribute WriteForm form, HttpSession session) throws IOException {
//        User sessionUser = (User) session.getAttribute("loginUser");
//        writeService.saveWrite(form, sessionUser, true);
//        return "redirect:/";
//    }
}
