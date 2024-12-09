package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.Post;
import com.github.jkky_98.noteJ.domain.PostTag;
import com.github.jkky_98.noteJ.domain.Tag;
import com.github.jkky_98.noteJ.repository.PostRepository;
import com.github.jkky_98.noteJ.repository.PostTagRepository;
import com.github.jkky_98.noteJ.repository.TagRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class PostServiceTest {

    @Autowired private PostService postService;

    @Autowired private PostRepository postRepository;
    @Autowired private PostTagRepository postTagRepository;
    @Autowired private TagRepository tagRepository;

    @Autowired private EntityManager em;

    private Post testPost;
    private Tag tag1;
    private Tag tag2;
    private PostTag postTag1;
    private PostTag postTag2;

    @BeforeEach
    void setUp() {
        // 게시글 생성 및 저장
        // 연관관계 매핑
        testPost = Post.builder()
                .title("Test Title")
                .build();
        postRepository.save(testPost);

        tag1 = Tag.builder()
                .name("Test Tag1").build();
        tag2 = Tag.builder()
                .name("Test Tag2").build();

        postTag1 = PostTag.builder()
                .post(testPost)
                .build();

        postTag2 = PostTag.builder()
                .post(testPost)
                .build();
    }

    @Test
    @DisplayName("[PostRepository] Post 삭제시 그에 딸린 Tag 자동 삭제(")
    void postDeletionRemovesAssociatedTags() {
        // given
        tag1.addPostTag(postTag1);
        tag2.addPostTag(postTag2);
        testPost.addPostTag(postTag1);
        testPost.addPostTag(postTag2);

        // 연관관계 주인 우선 영속화
        postTagRepository.saveAll(List.of(postTag1, postTag2));

        Post savedPost = postRepository.save(testPost);
        tagRepository.saveAll(List.of(tag1, tag2));

        // when
        // post 삭제
        postService.removePost(savedPost.getId());

        // then
        // 1. Post가 삭제되었는지 확인
        Optional<Post> deletedPost = postRepository.findById(savedPost.getId());
        assertThat(deletedPost).isEmpty();

        // 2. PostTag가 삭제되었는지 확인
        List<PostTag> remainingPostTags = postTagRepository.findAll();
        System.out.println("remainingPostTags = " + remainingPostTags);
        assertThat(remainingPostTags).isEmpty();

        // 3. 연관된 Tag가 삭제되었는지 확인
        List<Tag> remainingTags = tagRepository.findAll();
        System.out.println("remainingTags = " + remainingTags);
        assertThat(remainingTags).isEmpty();
    }

    @Test
    @DisplayName("[PostRepository] Post에 딸린 Tag 1개삭제")
    void removeSpecificTagFromPostTest() {
        // given
        tag1.addPostTag(postTag1);
        tag2.addPostTag(postTag2);
        testPost.addPostTag(postTag1);
        testPost.addPostTag(postTag2);

        // 연관관계 주인 우선 영속화
        postTagRepository.saveAll(List.of(postTag1, postTag2));

        Post savedPost = postRepository.save(testPost);
        tagRepository.saveAll(List.of(tag1, tag2));

        // when
        postService.removeTagInPost(testPost.getId(), tag1.getId());

        // then
        // 1. Post 존재 확인
        Optional<Post> deletedPost = postRepository.findById(savedPost.getId());
        assertThat(deletedPost).isNotEmpty();

        // 2. PostTag가 삭제되었는지 확인
        List<PostTag> remainingPostTags = postTagRepository.findAll();
        assertThat(remainingPostTags.size()).isEqualTo(1);

        // 3. 연관된 Tag가 삭제되었는지 확인
        List<Tag> remainingTags = tagRepository.findAll();
        assertThat(remainingTags.size()).isEqualTo(1);
        assertThat(remainingTags.get(0)).isEqualTo(tag2);
    }
}
