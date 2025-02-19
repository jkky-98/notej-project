package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.Comment;
import com.github.jkky_98.noteJ.domain.Post;
import com.github.jkky_98.noteJ.domain.mapper.CommentMapper;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.repository.CommentRepository;
import com.github.jkky_98.noteJ.web.controller.form.CommentForm;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("[CommentService] Unit Tests")
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostService postService;

    @Mock
    private UserService userService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private CommentService commentService;

    private User sessionUser;
    private Post post;

    @BeforeEach
    void setUp() {
        // 빌더를 사용하여 기본 사용자와 포스트 생성
        sessionUser = User.builder()
                .id(1L)
                .username("sessionUser")
                .build();

        User postAuthor = User.builder()
                .id(2L)
                .username("postAuthor")
                .build();

        post = Post.builder()
                .user(postAuthor)
                .title("Test Post Title")
                .build();
    }

    @Test
    @DisplayName("[CommentService] - saveComment: 일반 댓글 저장 (부모 없음)")
    void testSaveComment_NormalComment_NoParent() {
        // given
        Long sessionUserId = sessionUser.getId();
        String postUrl = "test-post-url";
        String username = sessionUser.getUsername();

        CommentForm commentForm = new CommentForm();
        commentForm.setContent("This is a test comment");
        commentForm.setParentsId(null);

        // stub 미리 설정
        when(userService.findUserById(sessionUserId)).thenReturn(sessionUser);
        when(postService.findByPostUrl(postUrl)).thenReturn(post);
        when(userService.findUserByUsername(post.getUser().getUsername()))
                .thenReturn(post.getUser()); // 수정된 부분

        Comment commentForSave = Comment.builder()
                .content(commentForm.getContent())
                .build();

        when(commentMapper.toCommentForSave(eq(post), eq(sessionUser), eq(commentForm.getContent()), any()))
                .thenReturn(commentForSave);

        // when
        commentService.saveComment(commentForm, sessionUserId, postUrl, username);

        // then
        verify(commentRepository).save(commentForSave);
        verify(notificationService, never()).sendCommentParentsNotification(any(), any(), any());
        verify(notificationService).sendCommentPostNotification(eq(post.getUser()), any(), eq(post.getTitle()));
    }

    @Test
    @DisplayName("[CommentService] - saveComment: 대댓글 저장")
    void testSaveComment_ReplyComment() {
        // given
        Long sessionUserId = 3L;
        String postUrl = "test-post-url";
        String username = "replyUser";

        CommentForm commentForm = new CommentForm();
        commentForm.setContent("This is a reply comment");
        commentForm.setParentsId(10L);

        User replyUser = User.builder()
                .id(sessionUserId)
                .username(username)
                .build();

        // 부모 댓글 작성자(실제 알림 호출에서는 post의 작성자를 대상으로 함)
        User parentUser = User.builder()
                .id(4L)
                .username("parentUser")
                .build();

        Comment parentComment = Comment.builder()
                .id(10L)
                .user(parentUser)
                .build();

        when(userService.findUserById(sessionUserId)).thenReturn(replyUser);
        when(postService.findByPostUrl(postUrl)).thenReturn(post);
        when(commentRepository.findById(10L)).thenReturn(Optional.of(parentComment));
        when(userService.findUserByUsername(post.getUser().getUsername()))
                .thenReturn(post.getUser()); // 게시글 작성자 반환

        Comment commentForSave = Comment.builder()
                .content(commentForm.getContent())
                .build();

        when(commentMapper.toCommentForSave(eq(post), eq(replyUser), eq(commentForm.getContent()), any()))
                .thenReturn(commentForSave);

        // when
        commentService.saveComment(commentForm, sessionUserId, postUrl, username);

        // then
        verify(commentRepository).save(commentForSave);
        // 실제 호출에서는 부모 댓글 알림 시 post의 작성자(즉, post.getUser())를 대상으로 함.
        verify(notificationService).sendCommentParentsNotification(eq(post.getUser()), any(), eq(post.getTitle()));
        verify(notificationService).sendCommentPostNotification(eq(post.getUser()), any(), eq(post.getTitle()));
    }

    @Test
    @DisplayName("[CommentService] - deleteComment: 정상 삭제")
    void testDeleteComment_SuccessfulDeletion() {
        // given
        Long commentId = 100L;
        Long userId = sessionUser.getId();

        when(userService.findUserById(userId)).thenReturn(sessionUser);

        Comment comment = Comment.builder()
                .id(commentId)
                .user(sessionUser)
                .build();

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        // when
        commentService.deleteComment(commentId, userId);

        // then
        verify(commentRepository).deleteById(commentId);
    }

    @Test
    @DisplayName("[CommentService] - deleteComment: 작성자 불일치 예외 발생")
    void testDeleteComment_UserMismatch_ShouldThrowException() {
        // given
        Long commentId = 100L;
        Long userId = sessionUser.getId();

        User otherUser = User.builder()
                .id(999L)
                .username("otherUser")
                .build();

        when(userService.findUserById(userId)).thenReturn(sessionUser);

        Comment comment = Comment.builder()
                .id(commentId)
                .user(otherUser)
                .build();

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> commentService.deleteComment(commentId, userId));

        assertEquals("댓글의 유저가 일치하지 않습니다.", exception.getMessage());
        verify(commentRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("[CommentService] - deleteComment: 댓글 미존재 예외 발생")
    void testDeleteComment_CommentNotFound_ShouldThrowException() {
        // given
        Long commentId = 100L;
        Long userId = sessionUser.getId();

        when(userService.findUserById(userId)).thenReturn(sessionUser);
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(EntityNotFoundException.class,
                () -> commentService.deleteComment(commentId, userId));
        verify(commentRepository, never()).deleteById(anyLong());
    }
}
