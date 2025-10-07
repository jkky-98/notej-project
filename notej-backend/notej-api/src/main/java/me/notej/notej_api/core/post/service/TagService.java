package me.notej.notej_api.core.post.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.notej.notej_api.core.post.domain.Post;
import me.notej.notej_api.core.post.domain.PostTag;
import me.notej.notej_api.core.post.domain.Tag;
import me.notej.notej_api.core.post.dto.TagResponse;
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

    @Transactional(readOnly = true)
    public List<TagResponse> getTags(final String blogUrl) {
        return tagRepository.findTagResponseByBlogUrl(blogUrl);
    }

    @Transactional
    public void saveTag(String tagName, Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("POST_NOT_FOUND"));

        // 태그 존재하는 지 찾고 없으면 태그 만들기
        Tag tag = null;
        if (tagRepository.existsByName(tagName)) {
            tag = tagRepository.findTagByName(tagName).orElseThrow(() -> new EntityNotFoundException("TAG_NOT_FOUND"));
        } else {
            tag = Tag.builder()
                    .name(tagName)
                    .build();
        }

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
        PostTag postTag = postTagRepository.findById(postTagId)
                .orElseThrow(() -> new EntityNotFoundException("POST_TAG_NOT_FOUND"));
        Tag tag = postTag.getTag();

        // PostTag 관계 삭제
        postTagRepository.delete(postTag);

        // 해당 태그를 사용하는 다른 PostTag가 없는 경우에만 Tag 삭제
        long tagUsageCount = postTagRepository.countByTagId(tag.getId());
        if (tagUsageCount == 0) {
            tagRepository.delete(tag);
        }
    }

    @Transactional
    public void updateTags(List<String> requestTags, Long postId) {

        List<String> addedTagsForLog = new ArrayList<>();
        List<String> removedTagsForLog = new ArrayList<>();

        Post post = postRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("POST_NOT_FOUND"));
        // 현재 포스트에 연결된 모든 태그 (이름, 포스트태그 엔티티)의 목록
        Map<String, PostTag> currentPostTagsMap = post.getPostTags().stream()
                .collect(Collectors.toMap(
                        pt -> pt.getTag().getName(), // 키: 태그 이름
                        pt -> pt                     // 값: PostTag 엔티티
                ));

        Set<String> requestTagNameSet = new HashSet<>(requestTags);
        // 삭제 로직
        /**
         * new 태그 이름 셋에 현재 태그 이름이 존재하지 않을 경우
         * 현재 태그를 삭제하는 로직
         * 삭제 대상 : 해당하는 postTag 엔티티
         * 삭제 대상(조건) : postTag가 제거된 이후 postTag에 연결된 Tag가 단 하나의 postTag도 안가지면 Tag도 삭제
         */
        for (String currentTagName : new ArrayList<>(currentPostTagsMap.keySet())) {
            if (!requestTagNameSet.contains(currentTagName)) {
                PostTag postTagToRemove = currentPostTagsMap.get(currentTagName);
                postTagRepository.delete(postTagToRemove);

                // 만약 태그가 postTag를 하나도 가지지 않는다면 tag엔티티도 삭제
                if (postTagRepository.countByTagId(postTagToRemove.getTag().getId()) == 0) {
                    tagRepository.delete(postTagToRemove.getTag());
                }
                removedTagsForLog.add(currentTagName);
            }
        }
        // 추가 로직
        for (String reqTag : requestTagNameSet) {
            if (!currentPostTagsMap.containsKey(reqTag)) {
                saveTag(reqTag, postId);

                // 로그 기록
                addedTagsForLog.add(reqTag);
            }
        }
        // 최종 업데이트 결과 로깅
        log(postId, addedTagsForLog, removedTagsForLog);
    }

    private static void log(Long postId, List<String> addedTagsForLog, List<String> removedTagsForLog) {
        if (!addedTagsForLog.isEmpty() || !removedTagsForLog.isEmpty()) {
            log.info("[TagService][updateTags]게시글 {}의 태그 업데이트 결과:", postId);
            if (!addedTagsForLog.isEmpty()) {
                log.info("[TagService][updateTags]  추가된 태그: {}", addedTagsForLog);
            }
            if (!removedTagsForLog.isEmpty()) {
                log.info("[TagService][updateTags]  삭제된 태그: {}", removedTagsForLog);
            }
        } else {
            log.info("[TagService][updateTags]게시글 {}의 태그 변경사항 없음.", postId);
        }
    }
}
