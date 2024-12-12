package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.Post;
import com.github.jkky_98.noteJ.domain.PostTag;
import com.github.jkky_98.noteJ.domain.Tag;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.repository.PostRepository;
import com.github.jkky_98.noteJ.repository.PostTagRepository;
import com.github.jkky_98.noteJ.repository.TagRepository;
import com.github.jkky_98.noteJ.repository.UserRepository;
import com.github.jkky_98.noteJ.web.controller.dto.PostDto;
import com.github.jkky_98.noteJ.web.controller.dto.PostViewDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    private final TagRepository tagRepository;

    private final PostTagRepository postTagRepository;

    private final UserRepository userRepository;

    @Transactional
    public PostViewDto getPost(String username, String postUrl) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new EntityNotFoundException("User not found"));

        List<Post> posts = user.getPosts();

        for (Post post : posts) {
            if (post.getPostUrl().equals(postUrl)) {
                PostViewDto postViewDto = new PostViewDto();
                postViewDto.setTitle(post.getTitle());
                postViewDto.setUsername(username);
                postViewDto.setContent(post.getContent());
                System.out.println(post.getContent());
                postViewDto.setCreateByDt(post.getCreateDt());
                postViewDto.setLikeCount(post.getLikes().size());

                List<String> tags = getTags(post);
                postViewDto.setTags(tags);
                return postViewDto;
            }
        }

        throw new EntityNotFoundException("Post not Found");
    }

    private static List<String> getTags(Post post) {
        List<String> tags = new ArrayList<>();
        for (PostTag postTag : post.getPostTags()) {
            tags.add(postTag.getTag().getName());
        }
        return tags;
    }

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
