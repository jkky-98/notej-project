package com.github.jkky_98.noteJ.web.controller;

import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.service.ContactService;
import com.github.jkky_98.noteJ.web.controller.form.ContactForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    @PostMapping("/contact")
    public String contact(
            @SessionAttribute("loginUser") Optional<User> sessionUser,
            @Validated @ModelAttribute ContactForm form,
            @RequestHeader("Referer") String referer
            ) {
        contactService.addContact(form, sessionUser);
        return "redirect:" + referer;
    }
}
