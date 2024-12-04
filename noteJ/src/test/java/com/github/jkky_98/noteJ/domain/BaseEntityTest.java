package com.github.jkky_98.noteJ.domain;

import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.domain.user.UserRole;
import com.github.jkky_98.noteJ.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class BaseEntityTest {

    @Autowired
    UserRepository userRepository;

    @Test
    @DisplayName("[BaseEntity] BaseEntity의 상속이 잘 이루어지는 지 체크")
    void saveTest() {
    	// given
        /**
         * User -> BaseEntity
         */
        User testUserEntity = User.builder()
                .username("testname")
                .email("test@gmail.com")
                .password("testpassword")
                .userRole(UserRole.USER)
                .build();
        // when
        User userTestSaved = userRepository.save(testUserEntity);
        User userTestFinded = userRepository.findById(userTestSaved.getId())
                .orElseThrow(() -> new RuntimeException("[TEST] User not found with ID: " + userTestSaved.getId()));
        // then
        assertThat(userTestFinded.getCreateBy()).isNotNull();

        assertThat(userTestFinded.getLastModifiedBy()).isNotNull();

        assertThat(userTestFinded.getCreateDt()).isNotNull();
        assertThat(userTestFinded.getCreateDt()).isInstanceOf(LocalDateTime.class);
        assertThat(userTestFinded.getLastModifiedDt()).isNotNull();
        assertThat(userTestFinded.getLastModifiedDt()).isInstanceOf(LocalDateTime.class);
    }

}
