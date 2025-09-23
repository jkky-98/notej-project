package me.notej.notej_api.core.post.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.notej.notej_api.core.post.domain.Post;
import me.notej.notej_api.core.post.domain.PostTag;
import me.notej.notej_api.core.post.domain.Tag;
import me.notej.notej_api.core.post.repository.PostRepository;
import me.notej.notej_api.core.post.repository.PostTagRepository;
import me.notej.notej_api.core.post.repository.TagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TagService {

    private final TagRepository tagRepository;
    private final PostTagRepository postTagRepository;
    private final PostRepository postRepository;

    @Transactional
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

        // 양뱡향 연관관계 메서드
        tag.addPostTag(postTag);
        post.addPostTag(postTag);

        log.info("[TagService][saveTag] TagService - saveTag() tag : {}", tag);
    }

    @Transactional(readOnly = true)
    public List<String> getTagsFromPost(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("POST_NOT_FOUND"));

        return post.getPostTags().stream()
                .map(pt -> pt.getTag().getName())
                .toList();
    }
    @Transactional
    public void deletePostTagAndTag(Long postTagId) {
        PostTag postTag = postTagRepository.findById(postTagId).orElseThrow(() -> new EntityNotFoundException("POST_TAG_NOT_FOUND"));
        Tag tag = postTag.getTag();

        postTagRepository.delete(postTag);
        tagRepository.delete(tag);
    }

    @Transactional
    public void updateTags(List<String> requestTags, Long postId) {

        List<String> addedTags = new ArrayList<>();
        List<String> removedTags = new ArrayList<>();

        Post post = postRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("POST_NOT_FOUND"));
        Map<String, PostTag> currentPostTagsMap = post.getPostTags().stream()
                .collect(Collectors.toMap(
                        pt -> pt.getTag().getName(), // 키: 태그 이름
                        pt -> pt                     // 값: PostTag 엔티티
                ));

        Set<String> requestTagNameSet = new HashSet<>(requestTags);
        // 삭제 로직
        for (String currentTag : new ArrayList<>(currentPostTagsMap.keySet())) {
            if (!requestTagNameSet.contains(currentTag)) {
                PostTag postTagToRemove = currentPostTagsMap.get(currentTag);
                postTagRepository.delete(postTagToRemove);

                //logging
                removedTags.add(currentTag);
            }
        }
        // 추가 로직
        for (String reqTag : requestTagNameSet) {
            if (!currentPostTagsMap.containsKey(reqTag)) {
                saveTag(reqTag, postId);

                // 로그 기록
                addedTags.add(reqTag);
            }
        }
        // 최종 업데이트 결과 로깅
        if (!addedTags.isEmpty() || !removedTags.isEmpty()) {
            log.info("[TagService][updateTags]게시글 {}의 태그 업데이트 결과:", postId);
            if (!addedTags.isEmpty()) {
                log.info("[TagService][updateTags]  추가된 태그: {}", addedTags);
            }
            if (!removedTags.isEmpty()) {
                log.info("[TagService][updateTags]  삭제된 태그: {}", removedTags);
            }
        } else {
            log.info("[TagService][updateTags]게시글 {}의 태그 변경사항 없음.", postId);
        }
    }
}
