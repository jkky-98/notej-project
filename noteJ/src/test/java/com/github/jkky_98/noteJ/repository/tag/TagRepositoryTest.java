package com.github.jkky_98.noteJ.repository.tag;

import com.github.jkky_98.noteJ.domain.Post;
import com.github.jkky_98.noteJ.domain.PostTag;
import com.github.jkky_98.noteJ.domain.Tag;
import com.github.jkky_98.noteJ.repository.PostRepository;
import com.github.jkky_98.noteJ.repository.PostTagRepository;
import com.github.jkky_98.noteJ.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TagRepositoryTest {

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostTagRepository postTagRepository;

    private Tag tag;
    private Post post;

    @BeforeEach
    void setup() {
        // 공통 데이터 초기화
        post = postRepository.save(Post.builder()
                .title("Test Post")
                .content("This is a test post")
                .build());

        tag = tagRepository.save(Tag.builder()
                .name("Test Tag")
                .build());
    }

    @Test
    @DisplayName("[TagRepository] 태그 저장 테스트")
    void saveTagTest() {
        // given
        Tag newTag = Tag.builder()
                .name("New Tag")
                .build();

        // when
        Tag savedTag = tagRepository.save(newTag);

        // then
        assertThat(savedTag.getId()).isNotNull();
        assertThat(savedTag.getName()).isEqualTo("New Tag");
    }

    @Test
    @DisplayName("[TagRepository] 태그 조회 테스트")
    void findTagByIdTest() {
        // when
        Optional<Tag> foundTag = tagRepository.findById(tag.getId());

        // then
        assertThat(foundTag).isPresent();
        assertThat(foundTag.get().getName()).isEqualTo("Test Tag");
    }

    @Test
    @DisplayName("[TagRepository] 태그 삭제 테스트")
    void deleteTagTest() {
        // when
        tagRepository.delete(tag);

        // then
        Optional<Tag> deletedTag = tagRepository.findById(tag.getId());
        assertThat(deletedTag).isEmpty();
    }

    @Test
    @DisplayName("[TagRepository] 태그와 PostTag 연관 관계 테스트")
    void addAndRemovePostTagTest() {
        // given
        PostTag postTag = PostTag.builder()
                .post(post)
                .tag(tag)
                .build();

        // 태그에 PostTag 추가
        tag.addPostTag(postTag);

        // when
        postTagRepository.save(postTag);

        // then
        // 태그에 추가된 PostTag 확인
        assertThat(tag.getPostTags()).contains(postTag);
        assertThat(postTag.getTag()).isEqualTo(tag);

        // 태그에서 PostTag 제거
        tag.removePostTag(postTag);

        // when
        postTagRepository.delete(postTag);

        // then
        assertThat(tag.getPostTags()).doesNotContain(postTag);
    }
}
