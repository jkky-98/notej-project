package com.github.jkky_98.noteJ.web.util;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile({"local", "test"})
@RequiredArgsConstructor
public class InitializerController {

    private final InitializerService initializerService;

    @PostMapping("/init")
    public ResponseEntity<?> init() {
        initializerService.initialize();
        return ResponseEntity.ok().build();
    }
}
