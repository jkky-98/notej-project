package me.notej.notej_api.core.post.controller;

import lombok.RequiredArgsConstructor;
import me.notej.notej_api.core.post.dto.TagResponse;
import me.notej.notej_api.core.post.service.TagService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class TagController {

    private final TagService tagService;

    @GetMapping("/{blogUrlName}/tags")
    public ResponseEntity<List<TagResponse>> getTags(
            @PathVariable String blogUrlName
    ) {
        List<TagResponse> tagResponses = tagService.getTags(blogUrlName);
        return ResponseEntity.ok(tagResponses);
    }
}
