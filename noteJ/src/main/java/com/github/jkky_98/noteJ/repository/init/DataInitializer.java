package com.github.jkky_98.noteJ.repository.init;

import com.github.jkky_98.noteJ.domain.FileMetadata;
import com.github.jkky_98.noteJ.domain.Post;
import com.github.jkky_98.noteJ.domain.Series;
import com.github.jkky_98.noteJ.domain.user.ThemeMode;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.domain.user.UserDesc;
import com.github.jkky_98.noteJ.domain.user.UserRole;
import com.github.jkky_98.noteJ.repository.PostRepository;
import com.github.jkky_98.noteJ.repository.SeriesRepository;
import com.github.jkky_98.noteJ.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Profile("temp")
public class DataInitializer {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final SeriesRepository seriesRepository;

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

            Series initSeries = Series.builder()
                    .seriesName("스프링 프로젝트 테스트")
                    .user(initSignUpUser)
                    .build();

            Post initPost1 = Post.builder()
                    .title("testPost1")
                    .content("testContent")
                    .writable(false)
                    .series(initSeries)
                    .user(initSignUpUser)
                    .thumbnail("/img/default_post.png")
                    .build();

            Post initPost2 = Post.builder()
                    .title("testPost2")
                    .content("testContent22")
                    .writable(false)
                    .series(initSeries)
                    .user(initSignUpUser)
                    .thumbnail("/img/default_post.png")
                    .build();


            userRepository.save(initSignUpUser);

            seriesRepository.save(initSeries);

            postRepository.save(initPost1);
            postRepository.save(initPost2);
        }

    }
}
