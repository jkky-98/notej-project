package com.github.jkky_98.noteJ.web.controller;

import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.service.WriteService;
import com.github.jkky_98.noteJ.web.controller.form.WriteForm;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Controller
@RequiredArgsConstructor
public class WriteController {

    private final WriteService writeService;

    @GetMapping("/write")
    public String write(
            HttpSession session,
            @ModelAttribute WriteForm writeForm,
            @RequestParam("title") String title,
            Model model) {
        User sessionUser = (User) session.getAttribute("loginUser");

        if (title.isEmpty()) {
            model.addAttribute("seriesList", writeService.getWrite(sessionUser));
        } else {
            WriteForm writeEditForm = writeService.getWriteEdit(sessionUser, title);
            model.addAttribute("writeForm", writeEditForm);
        }
        return "write";
    }

    @PostMapping("/write")
    public String writeSave(@ModelAttribute WriteForm form, HttpSession session, @RequestParam("title") String title) throws IOException {
        User sessionUser = (User) session.getAttribute("loginUser");

        if (!title.isEmpty()) {
            writeService.saveWrite(form, sessionUser, false);
        } else {
            // toDo: saveEditWrite 작성 필요
        }
        return "redirect:/";
    }

//    @PostMapping("/write/savetemp")
//    public String writeSaveTemp(@ModelAttribute WriteForm form, HttpSession session) throws IOException {
//        User sessionUser = (User) session.getAttribute("loginUser");
//        writeService.saveWrite(form, sessionUser, true);
//        return "redirect:/";
//    }
}
