package com.github.jkky_98.noteJ.domain;

import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.domain.user.UserDesc;
import com.github.jkky_98.noteJ.domain.user.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UserTest {

    @Test
    @DisplayName("[User] 생성 방식 테스트 : 빌더 패턴 적용")
    void UserBuilderTest() {
    	// given
        User testUser = createTestUser();
    	// when

    	// then
        assertThat(testUser.getUsername()).isEqualTo("testuser");
        assertThat(testUser.getEmail()).isEqualTo("test@example.com");
        assertThat(testUser.getPassword()).isEqualTo("password123");
        assertThat(testUser.getUserRole()).isEqualTo(UserRole.USER);
    }

    @Test
    @DisplayName("[User] 기본 상태 테스트")
    void userDefaultStateTest() {
        // given
        User testUser = User.builder()
                .build();

        // then
        assertThat(testUser).isNotNull();
        assertThat(testUser.getUsername()).isNull();
        assertThat(testUser.getEmail()).isNull();
        assertThat(testUser.getUserRole()).isNull();
        assertThat(testUser.getPassword()).isNull();
    }


    @Test
    @DisplayName("[User] @Builder.Default 초기화 테스트")
    void builderDefaultInitializationTest() {
        // given
        User testuser = createTestUser();

        // then
        // Posts 초기화 테스트
        assertThat(testuser.getPosts()).isNotNull();
        assertThat(testuser.getPosts().isEmpty()).isTrue();

        // SeriesList 초기화 테스트
        assertThat(testuser.getSeriesList()).isNotNull();
        assertThat(testuser.getSeriesList().isEmpty()).isTrue();

        // Likes 초기화 테스트
        assertThat(testuser.getLikes()).isNotNull();
        assertThat(testuser.getLikes().isEmpty()).isTrue();

        // Comments 초기화 테스트
        assertThat(testuser.getComments()).isNotNull();
        assertThat(testuser.getComments().isEmpty()).isTrue();

        // FollowingList 초기화 테스트
        assertThat(testuser.getFollowingList()).isNotNull();
        assertThat(testuser.getFollowingList().isEmpty()).isTrue();

        // FollowerList 초기화 테스트
        assertThat(testuser.getFollowerList()).isNotNull();
        assertThat(testuser.getFollowerList().isEmpty()).isTrue();

        assertThat(testuser.getSentNotifications()).isNotNull();
        assertThat(testuser.getSentNotifications().isEmpty()).isTrue();

    }

    @Test
    @DisplayName("[User] updateUserDesc 메서드 테스트")
    void updateUserDescTest() {
        User testUser = createTestUser();

        testUser.updateUserDesc(
                UserDesc.builder()
                        .description("updateDesc")
                        .blogTitle("updateTitle")
                        .build()
        );

        assertThat(testUser.getUserDesc()).isNotNull();
        assertThat(testUser.getUserDesc().getDescription()).isEqualTo("updateDesc");
        assertThat(testUser.getUserDesc().getBlogTitle()).isEqualTo("updateTitle");
    }

    @Test
    @DisplayName("[User] addPost 메서드 테스트")
    void addPostTest() {
        User testUser = createTestUser();
        testUser.addPost(
                Post.builder()
                        .title("testPostTitle")
                        .build()
        );

        assertThat(testUser.getPosts().size()).isEqualTo(1);
        assertThat(testUser.getPosts().get(0).getTitle()).isEqualTo("testPostTitle");
        assertThat(testUser.getPosts().get(0).getUser()).isEqualTo(testUser);
    }

    @Test
    @DisplayName("[User] addSeries 메서드 테스트")
    void addSeriesTest() {
        User testUser = createTestUser();
        testUser.addSeries(
                Series.builder()
                        .seriesName("testSeries")
                        .build()
        );

        assertThat(testUser.getSeriesList().size()).isEqualTo(1);
        assertThat(testUser.getSeriesList().get(0).getSeriesName()).isEqualTo("testSeries");
        assertThat(testUser.getSeriesList().get(0).getUser()).isEqualTo(testUser);
    }

    @Test
    @DisplayName("[User] addNotificationToRecipient 메서드 테스트")
    void addNotificationToRecipientTest() {
        User testUser = createTestUser();
        User senderUser = User.builder()
                .username("sendNotificationUser")
                .email("sendNotificationUser@gmail.com")
                .password("123456")
                .build();

        Notification testNotification = Notification.builder()
                .sender(senderUser)
                .receiver(testUser)
                .type(NotificationType.LIKE)
                .build();

        testUser.addNotificationToRecipient(testNotification);

        assertThat(testUser.getReceivedNotifications().size()).isEqualTo(1);
        assertThat(testUser.getReceivedNotifications().get(0).getSender()).isEqualTo(senderUser);
        assertThat(testUser.getReceivedNotifications().get(0).getReceiver()).isEqualTo(testUser);
    }

    @Test
    @DisplayName("[User] addNotificationToSender 메서드 테스트")
    void addNotificationToSenderTest() {
        User testUser = createTestUser();
        User recieverUser = User.builder()
                .username("sendNotificationUser")
                .email("sendNotificationUser@gmail.com")
                .password("123456")
                .build();

        Notification testNotification = Notification.builder()
                .sender(testUser)
                .receiver(recieverUser)
                .type(NotificationType.LIKE)
                .build();

        testUser.addNotificationToSender(testNotification);

        assertThat(testUser.getSentNotifications().size()).isEqualTo(1);
        assertThat(testUser.getSentNotifications().get(0).getSender()).isEqualTo(testUser);
        assertThat(testUser.getSentNotifications().get(0).getReceiver()).isEqualTo(recieverUser);
    }

    @Test
    @DisplayName("[User] isPasswordValid 메서드 테스트")
    void isPasswordValidTest() {
        // 테스트용 User 객체 생성 (빌더 사용)
        User testUser = createTestUser();

        // 올바른 비밀번호를 입력했을 경우
        assertThat(testUser.isPasswordValid("password123")).isTrue();

        // 잘못된 비밀번호를 입력했을 경우
        assertThat(testUser.isPasswordValid("wrongPassword")).isFalse();
    }


    private User createTestUser() {

        UserDesc userDesc = UserDesc.builder()
                .description("testdesc")
                .blogTitle("testblogtitle")
                .build();

        return User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .userRole(UserRole.USER)
                .userDesc(userDesc)
                .build();
    }

}
