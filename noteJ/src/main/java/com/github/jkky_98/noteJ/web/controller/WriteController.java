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
            @SessionAttribute("loginUser") User sessionUser
            ) {

        writeService.getWrite(writeForm, sessionUser.getId());
        return "write";
    }

    @GetMapping("/edit/{postUrl}")
    public String edit(
            @PathVariable("postUrl") String postUrl,
            @SessionAttribute("loginUser") User sessionUser,
            Model model
    ) {
        WriteForm writeEditForm = writeService.getWriteEdit(sessionUser.getId(), postUrl);
        model.addAttribute("writeForm", writeEditForm);
        return "edit";
    }


    @PostMapping("/write")
    public String writeSave(
            @ModelAttribute WriteForm form,
            @SessionAttribute("loginUser") User sessionUser,
            @RequestParam(value = "id" , required = false) Long postId
    ) throws IOException {

        writeService.saveWrite(form, sessionUser.getId());
        return "redirect:" + "/@" + sessionUser.getUsername() + "/posts";
    }

    @PostMapping("/edit/{postUrl}")
    public String editSave(
            @ModelAttribute WriteForm form,
            @SessionAttribute("loginUser") User sessionUser,
            @PathVariable String postUrl
    ) throws IOException {

        writeService.saveEditWrite(form, postUrl);
        return "redirect:" + "/@" + sessionUser.getUsername() + "/posts";
    }

}
