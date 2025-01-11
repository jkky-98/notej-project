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
            @ModelAttribute WriteForm writeForm,
            @RequestParam(value = "id", required = false) Long postId,
            @SessionAttribute("loginUser") User sessionUser,
            Model model) {

        if (postId == null) {
            writeService.getWrite(writeForm, sessionUser.getId());
        } else {
            WriteForm writeEditForm = writeService.getWriteEdit(sessionUser.getId(), postId);
            model.addAttribute("writeForm", writeEditForm);
        }
        return "write";
    }

    @PostMapping("/write")
    public String writeSave(
            @ModelAttribute WriteForm form,
            @SessionAttribute("loginUser") User sessionUser,
            @RequestParam(value = "id" , required = false) Long postId
    ) throws IOException {

        if (postId == null) {
            writeService.saveWrite(form, sessionUser.getId());
        } else {
            writeService.saveEditWrite(form, postId);
        }
        return "redirect:" + "/@" + sessionUser.getUsername() + "/posts";
    }

}
