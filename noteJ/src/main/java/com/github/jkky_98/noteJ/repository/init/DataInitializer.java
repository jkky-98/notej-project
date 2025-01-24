package com.github.jkky_98.noteJ.repository.init;

import com.github.jkky_98.noteJ.domain.*;
import com.github.jkky_98.noteJ.domain.user.ThemeMode;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.domain.user.UserDesc;
import com.github.jkky_98.noteJ.domain.user.UserRole;
import com.github.jkky_98.noteJ.repository.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Profile("local")
public class DataInitializer {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final SeriesRepository seriesRepository;
    private final PostTagRepository postTagRepository;
    private final TagRepository tagRepository;

    @PostConstruct
    public void init() {
        // 데이터베이스에 초기 데이터 삽입
        if (userRepository.count() == 0) {

            UserDesc initUserDesc = UserDesc.builder()
                    .blogTitle("TestTitle")
                    .socialEmail("example@google.com")
                    .commentAlarm(true)
                    .noteJAlarm(true)
                    .profilePic("default/default-profile.png")
                    .theme(ThemeMode.LIGHT)
                    .description("")
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

            Series initSeries2 = Series.builder()
                    .seriesName("헬로우 JPA")
                    .user(initSignUpUser)
                    .build();

            Series initSeries3 = Series.builder()
                    .seriesName("자바 고급 2")
                    .user(initSignUpUser)
                    .build();

            Post initPost1 = Post.builder()
                    .title("testPost1")
                    .content("testContent")
                    .writable(false)
                    .series(initSeries)
                    .user(initSignUpUser)
                    .postUrl("testPost1")
                    .build();

            Post initPost2 = Post.builder()
                    .title("testPost2")
                    .content("![image](https://uicdn.toast.com/toastui/img/tui-editor-bi.png)\n" +
                            "\n" +
                            "# Awesome Editor!\n" +
                            "\n" +
                            "It has been _released as opensource in 2018_ and has ~~continually~~ evolved to **receive 10k GitHub ⭐\uFE0F Stars**.\n" +
                            "\n" +
                            "## Create Instance\n" +
                            "\n" +
                            "You can create an instance with the following code and use `getHtml()` and `getMarkdown()` of the [Editor](https://github.com/nhn/tui.editor).\n" +
                            "\n" +
                            "```js\n" +
                            "const editor = new Editor(options);\n" +
                            "```\n" +
                            "\n" +
                            "> See the table below for default options\n" +
                            "> > More API information can be found in the document\n" +
                            "\n" +
                            "| name | type | description |\n" +
                            "| --- | --- | --- |\n" +
                            "| el | `HTMLElement` | container element |\n" +
                            "\n" +
                            "## Features\n" +
                            "\n" +
                            "* CommonMark + GFM Specifications\n" +
                            "   * Live Preview\n" +
                            "   * Scroll Sync\n" +
                            "   * Auto Indent\n" +
                            "   * Syntax Highlight\n" +
                            "        1. Markdown\n" +
                            "        2. Preview\n" +
                            "\n" +
                            "## Support Wrappers\n" +
                            "\n" +
                            "> * Wrappers\n" +
                            ">    1. [x] React\n" +
                            ">    2. [x] Vue\n" +
                            ">    3. [ ] Ember")
                    .writable(false)
                    .series(initSeries2)
                    .user(initSignUpUser)
                    .postUrl("testPost2")
                    .build();

            Tag tag1 = Tag.builder().name("spring").build();
            Tag tag2 = Tag.builder().name("java").build();
            Tag tag3 = Tag.builder().name("jpa").build();

            PostTag postTag1 = PostTag.builder().post(initPost1).tag(tag1).build();
            PostTag postTag2 = PostTag.builder().post(initPost2).tag(tag2).build();
            PostTag postTag3 = PostTag.builder().post(initPost2).tag(tag3).build();

            userRepository.save(initSignUpUser);

            seriesRepository.save(initSeries);
            seriesRepository.save(initSeries2);
            seriesRepository.save(initSeries3);

            for (int i = 0; i < 100; i++) {
                Post initPost = Post.builder()
                        .title("testPostfor" + i)
                        .content("testContent")
                        .writable(true)
                        .series(initSeries)
                        .user(initSignUpUser)
                        .postUrl("testPost-" + UUID.randomUUID())
                        .build();

                postRepository.save(initPost);
            }
            postRepository.save(initPost1);
            postRepository.save(initPost2);

            tagRepository.saveAll(List.of(tag1, tag2, tag3));
            postTagRepository.saveAll(List.of(postTag1, postTag2, postTag3));

        }

    }
}
