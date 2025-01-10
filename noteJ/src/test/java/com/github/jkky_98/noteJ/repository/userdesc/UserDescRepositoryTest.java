package com.github.jkky_98.noteJ.repository.userdesc;

import com.github.jkky_98.noteJ.domain.user.ThemeMode;
import com.github.jkky_98.noteJ.domain.user.UserDesc;
import com.github.jkky_98.noteJ.repository.UserDescRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class UserDescRepositoryTest {

    @Autowired
    UserDescRepository userDescRepository;

    @Test
    @DisplayName("[UserDescRepository] UserDesc 저장 테스트")
    void testSaveUserDesc() {
        // given
        UserDesc userDesc = UserDesc.builder()
                .description("Test Description")
                .profilePic("test-pic.png")
                .blogTitle("My Test Blog")
                .theme(ThemeMode.DARK)
                .socialEmail("test@example.com")
                .socialGitHub("https://github.com/test")
                .socialTwitter("https://twitter.com/test")
                .socialFacebook("https://facebook.com/test")
                .socialOther("https://example.com/test")
                .commentAlarm(true)
                .noteJAlarm(false)
                .build();

        // when
        UserDesc savedUserDesc = userDescRepository.save(userDesc);

        // then
        assertThat(savedUserDesc).isNotNull();
        assertThat(savedUserDesc.getId()).isNotNull();
        assertThat(savedUserDesc.getDescription()).isEqualTo("Test Description");
    }

    @Test
    @DisplayName("[UserDescRepository] UserDesc 조회 테스트")
    void testFindUserDescById() {
        // given
        UserDesc userDesc = UserDesc.builder()
                .description("Another Description")
                .profilePic("another-pic.png")
                .blogTitle("Another Blog")
                .theme(ThemeMode.LIGHT)
                .build();

        UserDesc savedUserDesc = userDescRepository.save(userDesc);

        // when
        Optional<UserDesc> foundUserDesc = userDescRepository.findById(savedUserDesc.getId());

        // then
        assertThat(foundUserDesc).isPresent();
        assertThat(foundUserDesc.get().getDescription()).isEqualTo("Another Description");
    }

    @Test
    @DisplayName("[UserDescRepository] UserDesc 삭제 테스트")
    void testDeleteUserDesc() {
        // given
        UserDesc userDesc = UserDesc.builder()
                .description("Delete Test")
                .build();

        UserDesc savedUserDesc = userDescRepository.save(userDesc);

        // when
        userDescRepository.delete(savedUserDesc);

        // then
        Optional<UserDesc> foundUserDesc = userDescRepository.findById(savedUserDesc.getId());
        assertThat(foundUserDesc).isNotPresent();
    }

    @Test
    @DisplayName("[UserDescRepository] UserDesc 리스트 조회 테스트")
    void testFindAllUserDescs() {
        // given
        UserDesc userDesc1 = UserDesc.builder()
                .description("First Description")
                .build();

        UserDesc userDesc2 = UserDesc.builder()
                .description("Second Description")
                .build();

        userDescRepository.save(userDesc1);
        userDescRepository.save(userDesc2);

        // when
        Iterable<UserDesc> userDescs = userDescRepository.findAll();

        // then
        assertThat(userDescs).hasSize(2);
    }

    @Test
    @DisplayName("[UserDescRepository] UserDesc 업데이트 테스트")
    void testUpdateUserDesc() {
        // given
        UserDesc userDesc = UserDesc.builder()
                .description("Original Description")
                .profilePic("original-pic.png")
                .build();

        UserDesc savedUserDesc = userDescRepository.save(userDesc);

        // when
        savedUserDesc.updateDescription("Updated Description");
        savedUserDesc.updateProfilePic("updated-pic.png");

        UserDesc updatedUserDesc = userDescRepository.save(savedUserDesc);

        // then
        assertThat(updatedUserDesc.getDescription()).isEqualTo("Updated Description");
        assertThat(updatedUserDesc.getProfilePic()).isEqualTo("updated-pic.png");
    }
}
