package com.franklintju.streamlab.videos;

import com.franklintju.streamlab.auth.AuthService;
import com.franklintju.streamlab.exceptions.VideoNotFoundException;
import com.franklintju.streamlab.upload.UploadTaskRepository;
import com.franklintju.streamlab.users.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VideoServiceTest {

    @Mock
    private VideoRepository videoRepository;
    @Mock
    private VideoConverter videoConverter;
    @Mock
    private UploadTaskRepository uploadTaskRepository;
    @Mock
    private AuthService authService;
    @Mock
    private VideoStatsRedisService videoStatsRedisService;
    @InjectMocks
    private VideoService videoService;

    private User owner;
    private Video video;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setEmail("owner@example.com");

        video = new Video();
        video.setId(100L);
        video.setTitle("Test Video");
        video.setUser(owner);
        video.setStatus(Video.VideoStatus.DRAFT);
    }

    @Test
    void shouldCreateVideo() {
        CreateVideoRequest request = new CreateVideoRequest();
        request.setTitle("New Video");
        request.setDescription("Description");

        when(authService.getCurrentUser()).thenReturn(owner);
        when(videoRepository.save(any(Video.class))).thenReturn(video);
        when(videoConverter.toDto(video)).thenReturn(new VideoDto());

        VideoDto result = videoService.createVideo(request);

        assertThat(result).isNotNull();
        verify(videoRepository).save(any(Video.class));
    }

    @Test
    void shouldUpdateVideoWhenOwner() {
        UpdateVideoRequest request = new UpdateVideoRequest();
        request.setTitle("Updated Title");
        request.setDescription("Updated Desc");
        request.setVideoUrl("http://example.com/video.mp4");

        when(authService.getCurrentUser()).thenReturn(owner);
        when(videoRepository.findById(100L)).thenReturn(Optional.of(video));
        when(videoRepository.save(any(Video.class))).thenReturn(video);
        when(videoConverter.toDto(video)).thenReturn(new VideoDto());

        VideoDto result = videoService.updateVideo(100L, request);

        assertThat(result).isNotNull();
        verify(videoRepository).save(video);
    }

    @Test
    void shouldThrowExceptionWhenUpdateByNonOwner() {
        User otherUser = new User();
        otherUser.setId(2L);

        UpdateVideoRequest request = new UpdateVideoRequest();

        when(authService.getCurrentUser()).thenReturn(otherUser);
        when(videoRepository.findById(100L)).thenReturn(Optional.of(video));

        assertThatThrownBy(() -> videoService.updateVideo(100L, request))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void shouldDeleteVideoWhenOwner() {
        when(authService.getCurrentUser()).thenReturn(owner);
        when(videoRepository.findById(100L)).thenReturn(Optional.of(video));

        videoService.deleteVideo(100L);

        verify(videoRepository).delete(video);
    }

    @Test
    void shouldThrowExceptionWhenDeleteByNonOwner() {
        User otherUser = new User();
        otherUser.setId(2L);

        when(authService.getCurrentUser()).thenReturn(otherUser);
        when(videoRepository.findById(100L)).thenReturn(Optional.of(video));

        assertThatThrownBy(() -> videoService.deleteVideo(100L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void shouldPublishVideoWhenReady() {
        video.setStatus(Video.VideoStatus.READY);

        when(authService.getCurrentUser()).thenReturn(owner);
        when(videoRepository.findById(100L)).thenReturn(Optional.of(video));
        when(videoConverter.toDto(video)).thenReturn(new VideoDto());

        VideoDto result = videoService.publishVideo(100L);

        assertThat(result).isNotNull();
    }

    @Test
    void shouldThrowExceptionWhenPublishNotReady() {
        when(authService.getCurrentUser()).thenReturn(owner);
        when(videoRepository.findById(100L)).thenReturn(Optional.of(video));

        assertThatThrownBy(() -> videoService.publishVideo(100L))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldIncrementViewCount() {
        when(videoRepository.findById(100L)).thenReturn(Optional.of(video));

        videoService.incrementViewCount(100L);

        assertThat(video.getViewsCount()).isEqualTo(1);
        verify(videoRepository).save(video);
    }

    @Test
    void shouldThrowExceptionWhenVideoNotFound() {
        when(videoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> videoService.getVideo(999L))
                .isInstanceOf(VideoNotFoundException.class);
    }

    @Test
    void shouldReturnPublicVideos() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Video> page = new PageImpl<>(List.of(video));

        when(videoRepository.findByStatus(Video.VideoStatus.PUBLIC, pageable)).thenReturn(page);
        when(videoConverter.toDto(video)).thenReturn(new VideoDto());

        Page<VideoDto> result = videoService.listVideos(0, 10);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void shouldReturnOwnerVideos() {
        when(authService.getCurrentUser()).thenReturn(owner);
        when(videoRepository.findByUserId(1L)).thenReturn(List.of(video));
        when(videoConverter.toDto(video)).thenReturn(new VideoDto());

        List<VideoDto> result = videoService.getVideosByUser(1L);

        assertThat(result).hasSize(1);
    }
}
