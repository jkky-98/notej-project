package com.github.jkky_98.noteJ.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class TagTest {

    @Test
    @DisplayName("[Tag] 빌더를 통한 객체 생성 테스트")
    void tagBuilderCreationTest() {
        // given: 테스트용 Tag 객체 생성
        Tag tag = createTestTag("Test Tag");

        // then: 각 필드가 올바르게 설정되었는지 검증
        assertThat(tag).isNotNull();
        assertThat(tag.getName()).isEqualTo("Test Tag");
        // @Builder.Default로 초기화한 컬렉션은 null이 아니며 빈 리스트여야 함
        assertThat(tag.getPostTags()).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("[Tag] 기본 상태 테스트")
    void tagDefaultStateTest() {
        // given: 아무 값도 설정하지 않고 빌더로 Tag 객체 생성
        Tag defaultTag = Tag.builder().build();

        // then: 기본 상태 검증
        // id와 name은 설정하지 않았으므로 null이어야 함
        assertThat(defaultTag.getId()).isNull();
        assertThat(defaultTag.getName()).isNull();
        // Builder.Default 컬렉션은 null이 아니며 빈 리스트여야 함
        assertThat(defaultTag.getPostTags()).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("[Tag] addPostTag 메서드 테스트")
    void addPostTagTest() {
        // given: 테스트용 Tag와 PostTag 객체 생성
        Tag tag = createTestTag("Initial Tag");
        PostTag postTag = createTestPostTag(null, null); // PostTag 생성 시 초기값

        // when: addPostTag 메서드를 호출하여 연관관계 설정
        tag.addPostTag(postTag);

        // then: Tag의 postTags 컬렉션에 postTag가 추가되고, postTag의 tag가 해당 Tag로 업데이트되어야 함
        assertThat(tag.getPostTags()).contains(postTag);
        assertThat(postTag.getTag()).isEqualTo(tag);
    }

    @Test
    @DisplayName("[Tag] removePostTag 메서드 테스트")
    void removePostTagTest() {
        // given: 테스트용 Tag와 PostTag 객체 생성 후, 관계 설정
        Tag tag = createTestTag("Initial Tag");
        PostTag postTag = createTestPostTag(null, null);
        tag.addPostTag(postTag);
        assertThat(tag.getPostTags()).contains(postTag);
        assertThat(postTag.getTag()).isEqualTo(tag);

        // when: removePostTag 메서드 호출하여 관계 해제
        tag.removePostTag(postTag);

        // then: Tag의 postTags 컬렉션에서 제거되고, postTag의 tag 필드가 null이어야 함
        assertThat(tag.getPostTags()).doesNotContain(postTag);
        assertThat(postTag.getTag()).isNull();
    }

    // 헬퍼 메서드: 테스트용 Tag 객체 생성
    private Tag createTestTag(String name) {
        return Tag.builder()
                .name(name)
                .build();
    }

    // 헬퍼 메서드: 테스트용 PostTag 객체 생성
    // Post와 Tag는 필요에 따라 설정하고, 여기서는 초기 연관관계는 null로 생성합니다.
    private PostTag createTestPostTag(Post post, Tag tag) {
        return PostTag.builder()
                .post(post)
                .tag(tag)
                .build();
    }
}