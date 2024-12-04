package com.github.jkky_98.noteJ.repository.init;

import com.github.jkky_98.noteJ.domain.FileMetadata;
import com.github.jkky_98.noteJ.domain.user.ThemeMode;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.domain.user.UserDesc;
import com.github.jkky_98.noteJ.domain.user.UserRole;
import com.github.jkky_98.noteJ.repository.UserDescRepository;
import com.github.jkky_98.noteJ.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;

    @PostConstruct
    public void init() {
        // 데이터베이스에 초기 데이터 삽입
        if (userRepository.count() == 0) {
            FileMetadata initFileMetadata = FileMetadata.builder()
                    .build();

            UserDesc initUserDesc = UserDesc.builder()
                    .blogTitle("TestTitle")
                    .socialEmail("example@google.com")
                    .commentAlarm(true)
                    .noteJAlarm(true)
                    .theme(ThemeMode.LIGHT)
                    .profilePic("img/default-profile.png")
                    .fileMetadata(initFileMetadata)
                    .build();

            User initSignUpUser = User.builder()
                    .username("testId")
                    .email("example@google.com")
                    .password("test@@")
                    .userRole(UserRole.USER)
                    .userDesc(initUserDesc)
                    .build();

            userRepository.save(initSignUpUser);
        }

    }
}
