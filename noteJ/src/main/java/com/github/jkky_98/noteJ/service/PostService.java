package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.Post;
import com.github.jkky_98.noteJ.domain.PostTag;
import com.github.jkky_98.noteJ.domain.Tag;
import com.github.jkky_98.noteJ.repository.PostRepository;
import com.github.jkky_98.noteJ.repository.PostTagRepository;
import com.github.jkky_98.noteJ.repository.TagRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    private final TagRepository tagRepository;

    private final PostTagRepository postTagRepository;

    @Transactional
    public void removePost(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("Post not found"));

        // 2. Post에 연결된 PostTag 처리
        for (PostTag postTag : new ArrayList<>(post.getPostTags())) {
            Tag tag = postTag.getTag();

            // PostTag 제거
            tag.removePostTag(postTag);
            post.removePostTag(postTag);

            // 고아 상태가 된 Tag 삭제
            if (tag.getPostTags().isEmpty()) {
                tagRepository.delete(tag);
            }
        }

        // 3. Post 삭제
        postRepository.delete(post);
    }

    @Transactional
    public void removeTagInPost(Long postId, Long tagId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("Post not found"));

        for (PostTag postTag : new ArrayList<>(post.getPostTags())) {
            if (postTag.getTag().getId().equals(tagId)) {
                // 양방향에서 연관관계 끊기
                Tag targetTag = postTag.getTag();
                targetTag.removePostTag(postTag);
                post.removePostTag(postTag);

                if (targetTag.getPostTags().isEmpty()) {
                    tagRepository.delete(targetTag);
                }
                return;
            }
        }
    }
}
