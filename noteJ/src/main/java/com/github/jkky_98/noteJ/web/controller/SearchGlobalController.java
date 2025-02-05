package com.github.jkky_98.noteJ.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SearchGlobalController {

    @GetMapping("/search-global")
    public String searchGlobal() {
        return "search";
    }
}
