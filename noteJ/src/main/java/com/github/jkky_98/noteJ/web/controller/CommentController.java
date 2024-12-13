package com.github.jkky_98.noteJ.web.controller;

import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.service.CommentService;
import com.github.jkky_98.noteJ.web.controller.form.CommentForm;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("@{postUsername}/post/{postUrl}/comments")
    public String writeComments(@PathVariable("postUsername") String postUsername,
                                @PathVariable("postUrl") String postUrl,
                                @SessionAttribute("loginUser") User sessionUser,
                                @ModelAttribute CommentForm commentForm,
                                HttpServletRequest request){

        commentService.saveComment(commentForm, sessionUser, postUrl, postUsername);
        String referer = request.getHeader("Referer");
        return "redirect:" + referer;
    }
}
