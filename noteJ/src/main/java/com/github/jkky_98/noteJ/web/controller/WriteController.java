package com.github.jkky_98.noteJ.web.controller;

import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.service.WriteService;
import com.github.jkky_98.noteJ.web.controller.form.WriteForm;
import com.github.jkky_98.noteJ.web.session.SessionConst;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
@Slf4j
@Controller
@RequiredArgsConstructor
public class WriteController {

    private final WriteService writeService;

    @GetMapping("/write")
    public String write(
            @ModelAttribute WriteForm writeForm,
            @SessionAttribute(SessionConst.LOGIN_USER) User sessionUser
            ) {

        writeService.getWrite(writeForm, sessionUser.getId());
        return "write";
    }

    @PostMapping("/write")
    public String writeSave(
            @Validated @ModelAttribute WriteForm writeForm,
            BindingResult bindingResult,
            @SessionAttribute(SessionConst.LOGIN_USER) User sessionUser
    ) throws IOException {
        if (bindingResult.hasErrors()) {
            return "write";
        }
        writeService.saveWrite(writeForm, sessionUser.getId());
        return "redirect:" + "/@" + sessionUser.getUsername() + "/posts";
    }

    @GetMapping("/edit/{postUrl}")
    public String edit(
            @PathVariable("postUrl") String postUrl,
            @SessionAttribute(SessionConst.LOGIN_USER) User sessionUser,
            Model model
    ) {
        WriteForm writeEditForm = writeService.getWriteEdit(sessionUser.getId(), postUrl);
        model.addAttribute("writeForm", writeEditForm);
        return "edit";
    }


    @PostMapping("/edit/{postUrl}")
    public String editSave(
            @Validated @ModelAttribute WriteForm form,
            @SessionAttribute(SessionConst.LOGIN_USER) User sessionUser,
            @PathVariable String postUrl,
            BindingResult bindResult
    ) throws IOException {
        if (bindResult.hasErrors()) {
            return "edit";
        }

        writeService.saveEditWrite(form, postUrl);
        return "redirect:" + "/@" + sessionUser.getUsername() + "/posts";
    }

}
