package me.notej.notej_api.core.post.controller;

import lombok.RequiredArgsConstructor;
import me.notej.notej_api.core.post.dto.EditorImageUploadResponse;
import me.notej.notej_api.core.post.dto.PostThumbnailImageUploadResponse;
import me.notej.notej_api.core.post.service.PostImageService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PostImageController {

    private final PostImageService postImageService;

    @PostMapping("/secure/editor/image")
    public ResponseEntity<EditorImageUploadResponse> uploadImage(
        Authentication authentication,
        @RequestPart(value = "image") MultipartFile imageFile
    ) {
        EditorImageUploadResponse res = postImageService.uploadImage(authentication, imageFile);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/editor/image")
    public byte[] getImage(
            @RequestParam String filename
    ) {
        return postImageService.downloadImage(filename);
    }

    @PostMapping("/secure/post/thumbnail")
    public ResponseEntity<PostThumbnailImageUploadResponse> uploadThumbnailImage(
            Authentication authentication,
            @RequestPart(value = "image") MultipartFile imageFile
    ) {
        PostThumbnailImageUploadResponse res = postImageService.uploadThumbnail(authentication, imageFile);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/secure/post/thumbnail")
    public byte[] getThumbnailImage(
            @RequestParam String filename
    ) {
        return postImageService.downloadImage(filename);
    }
}
