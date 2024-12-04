package com.github.jkky_98.noteJ.web.controller.form;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignUpForm {

    @NotBlank(message = "Username is required.")
    @Size(min = 2, max = 20, message = "Username must be between 2 and 20 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid format.")
    @Column(unique = true)
    private String email;

    @NotBlank(message = "Password is required.")
    @Size(min = 8, max = 100, message = "Password must be at least 8 characters long.")
    private String password;

    @NotBlank(message = "BlogTitle is required.")
    @Size(min = 2, max = 20, message = "Blog title must be between 2 and 20 characters")
    private String blogTitle;

}
