package com.github.jkky_98.noteJ.web.controller.form;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class ContactForm {

    @Email
    private String email;

    private String content;
}
