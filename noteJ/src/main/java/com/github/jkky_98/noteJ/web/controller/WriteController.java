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
            @RequestParam(value = "title", required = false) String title,
            Model model) {
        User sessionUser = (User) session.getAttribute("loginUser");

        if (title == null || title.isEmpty()) {
            model.addAttribute("seriesList", writeService.getSeriesWithUser(sessionUser));
        } else {
            WriteForm writeEditForm = writeService.getWriteEdit(sessionUser, title);
            model.addAttribute("writeForm", writeEditForm);
            model.addAttribute("seriesList", writeService.getSeriesWithUser(sessionUser));
        }
        return "write";
    }

    @PostMapping("/write")
    public String writeSave(@ModelAttribute WriteForm form, HttpSession session, @RequestParam("title") String title) throws IOException {
        User sessionUser = (User) session.getAttribute("loginUser");

        if (!title.isEmpty()) {
            writeService.saveWrite(form, sessionUser, false);
        } else {
            writeService.saveEditWrite(form, title);
        }
        return "redirect:/";
    }

}
