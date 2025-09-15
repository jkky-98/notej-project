package me.notej.notej_api.core.post.controller;

import lombok.RequiredArgsConstructor;
import me.notej.notej_api.core.post.dto.EditorImageUploadResponse;
import me.notej.notej_api.core.post.service.EditorImageService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class WriteEditorImageController {

    private final EditorImageService editorImageService;

    @PostMapping("/editor/image")
    public ResponseEntity<EditorImageUploadResponse> uploadImage(
        Authentication authentication,
        @RequestPart(value = "image") MultipartFile imageFile
    ) {
        EditorImageUploadResponse res = editorImageService.uploadImage(authentication, imageFile);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/editor/image")
    public byte[] getImage(
            @RequestParam String filename
    ) {
        return editorImageService.downloadImage(filename);
    }
}
