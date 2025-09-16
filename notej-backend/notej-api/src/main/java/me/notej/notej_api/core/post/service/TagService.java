package me.notej.notej_api.core.post.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.notej.notej_api.core.post.domain.Post;
import me.notej.notej_api.core.post.domain.PostTag;
import me.notej.notej_api.core.post.domain.Tag;
import me.notej.notej_api.core.post.repository.PostRepository;
import me.notej.notej_api.core.post.repository.PostTagRepository;
import me.notej.notej_api.core.post.repository.TagRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TagService {

    private final TagRepository tagRepository;
    private final PostTagRepository postTagRepository;
    private final PostRepository postRepository;

    public void saveTag(String tagName, Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("POST_NOT_FOUND"));

        Tag tag = Tag.builder()
                .name(tagName)
                .build();

        PostTag postTag = PostTag.builder()
                .tag(tag)
                .post(post)
                .build();

        tagRepository.save(tag);
        postTagRepository.save(postTag);
        log.info("[TagService][saveTag] TagService - saveTag() tag : {}", tag);
    }


}
