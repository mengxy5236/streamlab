package com.franklintju.streamlab.comment;

import com.franklintju.streamlab.users.User;
import com.franklintju.streamlab.users.UserRepository;
import com.franklintju.streamlab.videos.Video;
import com.franklintju.streamlab.videos.VideoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private VideoRepository videoRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private CommentService commentService;

    private User user;
    private Video video;
    private Comment comment;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");

        video = new Video();
        video.setId(100L);
        video.setTitle("Test Video");

        comment = new Comment();
        comment.setId(1L);
        comment.setUser(user);
        comment.setVideo(video);
        comment.setContent("Test comment");
        comment.setStatus(Comment.CommentStatus.VISIBLE);
    }

    @Test
    void shouldCreateRootComment() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(videoRepository.findById(100L)).thenReturn(Optional.of(video));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        Comment result = commentService.createComment(1L, 100L, "Test comment", null, null);

        assertThat(result.getContent()).isEqualTo("Test comment");
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void shouldCreateReplyComment() {
        Comment parentComment = new Comment();
        parentComment.setId(10L);
        parentComment.setUser(user);
        parentComment.setReplyCount(0);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(videoRepository.findById(100L)).thenReturn(Optional.of(video));
        when(commentRepository.findById(10L)).thenReturn(Optional.of(parentComment));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment c = invocation.getArgument(0);
            c.setId(2L);
            return c;
        });

        Comment result = commentService.createComment(1L, 100L, "Reply content", 10L, null);

        assertThat(result.getContent()).isEqualTo("Reply content");
        assertThat(result.getParent()).isEqualTo(parentComment);
        verify(commentRepository).updateReplyCount(10L, 1);
    }

    @Test
    void shouldUpdateComment() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        Comment result = commentService.updateComment(1L, "Updated content");

        assertThat(result.getContent()).isEqualTo("Updated content");
        verify(commentRepository).save(comment);
    }

    @Test
    void shouldDeleteComment() {
        Comment parentComment = new Comment();
        parentComment.setId(10L);
        comment.setParent(parentComment);

        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        commentService.deleteComment(1L);

        verify(commentRepository).delete(comment);
    }

    @Test
    void shouldReturnVideoComments() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Comment> page = new PageImpl<>(List.of(comment));

        when(commentRepository.findByVideoIdAndRootIsNullAndStatus(100L, Comment.CommentStatus.VISIBLE, pageable)).thenReturn(page);

        Page<Comment> result = commentService.getVideoComments(100L, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void shouldReturnCommentCount() {
        when(commentRepository.countByVideoIdAndStatus(100L, Comment.CommentStatus.VISIBLE)).thenReturn(5L);

        long count = commentService.getCommentCount(100L);

        assertThat(count).isEqualTo(5L);
    }
}
