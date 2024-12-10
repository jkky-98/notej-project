package com.github.jkky_98.noteJ.repository.posttag;

import com.github.jkky_98.noteJ.domain.Post;
import com.github.jkky_98.noteJ.domain.PostTag;
import com.github.jkky_98.noteJ.domain.Tag;
import com.github.jkky_98.noteJ.repository.PostRepository;
import com.github.jkky_98.noteJ.repository.PostTagRepository;
import com.github.jkky_98.noteJ.repository.TagRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class PostTagRepositoryTest {

    @Autowired
    private PostTagRepository postTagRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private TagRepository tagRepository;

    private Post post;
    private Tag tag;

    @BeforeEach
    void setup() {
        // 공통적으로 사용하는 Post와 Tag를 저장
        post = postRepository.save(Post.builder()
                .title("Test Post")
                .content("This is a test post")
                .build());

        tag = tagRepository.save(Tag.builder()
                .name("Test Tag")
                .build());
    }

    @Test
    @DisplayName("[PostTagRepository] PostTag 저장 테스트")
    void savePostTagTest() {
        // given
        PostTag postTag = PostTag.builder()
                .post(post)
                .tag(tag)
                .build();

        // when
        PostTag savedPostTag = postTagRepository.save(postTag);

        // then
        assertThat(savedPostTag.getId()).isNotNull();
        assertThat(savedPostTag.getPost()).isEqualTo(post);
        assertThat(savedPostTag.getTag()).isEqualTo(tag);
    }

    @Test
    @DisplayName("[PostTagRepository] PostTag 조회 테스트")
    void findPostTagByIdTest() {
        // given
        PostTag postTag = postTagRepository.save(PostTag.builder()
                .post(post)
                .tag(tag)
                .build());

        // when
        Optional<PostTag> foundPostTag = postTagRepository.findById(postTag.getId());

        // then
        assertThat(foundPostTag).isPresent();
        assertThat(foundPostTag.get().getPost()).isEqualTo(post);
        assertThat(foundPostTag.get().getTag()).isEqualTo(tag);
    }

    @Test
    @DisplayName("[PostTagRepository] PostTag 삭제 테스트")
    void deletePostTagTest() {
        // given
        PostTag postTag = postTagRepository.save(PostTag.builder()
                .post(post)
                .tag(tag)
                .build());

        // when
        postTagRepository.delete(postTag);

        // then
        Optional<PostTag> deletedPostTag = postTagRepository.findById(postTag.getId());
        assertThat(deletedPostTag).isEmpty();
    }

    @Test
    @DisplayName("[PostTagRepository] 특정 게시물의 모든 PostTag 조회 테스트")
    void findAllByPostTest() {
        // given
        Tag tag1 = tagRepository.save(Tag.builder().name("Tag1").build());
        Tag tag2 = tagRepository.save(Tag.builder().name("Tag2").build());

        postTagRepository.save(PostTag.builder().post(post).tag(tag1).build());
        postTagRepository.save(PostTag.builder().post(post).tag(tag2).build());

        // when
        List<PostTag> postTags = postTagRepository.findAll();

        // then
        assertThat(postTags).hasSize(2);
        assertThat(postTags).extracting("post").containsOnly(post);
        assertThat(postTags).extracting("tag").containsExactlyInAnyOrder(tag1, tag2);
    }
}
