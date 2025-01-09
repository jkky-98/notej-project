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
            @RequestParam(value = "id", required = false) Long postId,
            Model model) {
        User sessionUser = (User) session.getAttribute("loginUser");
        if (postId == null) {
            model.addAttribute("seriesList", writeService.getSeriesWithUser(sessionUser));
        } else {
            WriteForm writeEditForm = writeService.getWriteEdit(sessionUser, postId);
            model.addAttribute("writeForm", writeEditForm);
            model.addAttribute("seriesList", writeService.getSeriesWithUser(sessionUser));
        }
        return "write";
    }

    @PostMapping("/write")
    public String writeSave(@ModelAttribute WriteForm form, HttpSession session, @RequestParam(value = "id" , required = false) Long postId) throws IOException {
        User sessionUser = (User) session.getAttribute("loginUser");
        System.out.println("fasdgasdgas");
        System.out.println(postId);
        if (postId == null) {
            writeService.saveWrite(form, sessionUser, false);
        } else {
            writeService.saveEditWrite(form, postId);
        }
        return "redirect:/";
    }

}
