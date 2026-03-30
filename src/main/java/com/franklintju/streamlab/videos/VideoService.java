package com.franklintju.streamlab.videos;

import com.franklintju.streamlab.auth.AuthService;
import com.franklintju.streamlab.exceptions.VideoNotFoundException;
import com.franklintju.streamlab.upload.UploadTask;
import com.franklintju.streamlab.upload.UploadTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Service
public class VideoService {

    private final VideoRepository videoRepository;
    private final VideoConverter videoConverter;
    private final UploadTaskRepository uploadTaskRepository;
    private final AuthService authService;
    private final VideoStatsRedisService videoStatsRedisService;
    private final ViewEventProducer viewEventProducer;


    @Transactional
    public VideoDto updateVideo(Long id, UpdateVideoRequest request) {

        var video = videoRepository.findById(id).orElseThrow(VideoNotFoundException::new);

        Long currentUserId = authService.getCurrentUser().getId();
        if (!video.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("无权修改此视频");
        }

        boolean videoUrlChanged = !Objects.equals(video.getVideoUrl(), request.getVideoUrl());

        video.setTitle(request.getTitle());
        video.setDescription(request.getDescription());
        video.setCoverUrl(request.getCoverUrl());
        video.setVideoUrl(request.getVideoUrl());

        if (videoUrlChanged) {
            video.setStatus(Video.VideoStatus.READY);

            if (!uploadTaskRepository.existsByVideoId(video.getId())) {
                UploadTask task = new UploadTask();
                task.setUserId(video.getUser().getId());
                task.setVideoId(video.getId());
                uploadTaskRepository.save(task);
            }
        }
        return videoConverter.toDto(video);
    }

    public List<VideoDto> getVideosByUser(Long userId) {
        Long currentUserId = authService.getCurrentUser() != null 
                ? authService.getCurrentUser().getId() 
                : null;
        
        boolean isOwner = currentUserId != null && currentUserId.equals(userId);
        
        if (isOwner) {
            return videoRepository.findByUserId(userId)
                    .stream()
                    .map(videoConverter::toDto)
                    .toList();
        }
        
        return videoRepository.findByUserIdAndStatus(userId, Video.VideoStatus.PUBLIC)
                .stream()
                .map(videoConverter::toDto)
                .toList();
    }

    public VideoDto getVideo(Long id) {
        var video = videoRepository.findById(id).orElseThrow(VideoNotFoundException::new);
        
        if (video.getStatus() != Video.VideoStatus.PUBLIC) {
            Long currentUserId = authService.getCurrentUser() != null 
                    ? authService.getCurrentUser().getId() 
                    : null;
            if (currentUserId == null || !video.getUser().getId().equals(currentUserId)) {
                throw new VideoNotFoundException();
            }
        }
        
        return videoConverter.toDto(video);
    }

    @Transactional
    public VideoDto createVideo(CreateVideoRequest request) {
        var user = authService.getCurrentUser();
        var video = new Video();
        video.setUser(user);
        video.setTitle(request.getTitle());
        video.setDescription(request.getDescription());
        video.setCoverUrl(request.getCoverUrl());
        video.setStatus(Video.VideoStatus.DRAFT);
        videoRepository.save(video);
        return videoConverter.toDto(video);
    }

    @Transactional
    public void deleteVideo(Long id) {
        var video = videoRepository.findById(id).orElseThrow(VideoNotFoundException::new);
        Long currentUserId = authService.getCurrentUser().getId();
        if (!video.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("无权删除此视频");
        }
        videoRepository.delete(video);
    }

    @Transactional
    public void incrementViewCount(Long id) {
        if (!videoRepository.existsById(id)) {
            throw new VideoNotFoundException();
        }
        videoStatsRedisService.incrementViews(id, 1);
        viewEventProducer.sendViewEvent(new ViewEventMessage(id, null, 1L, java.time.Instant.now()));
    }

    public Page<VideoDto> listVideos(int page, int size) {
        var pageable = PageRequest.of(page, size);
        return videoRepository.findByStatus(Video.VideoStatus.PUBLIC, pageable)
                .map(videoConverter::toDto);
    }

    @Transactional
    public VideoDto publishVideo(Long id) {
        var video = videoRepository.findById(id).orElseThrow(VideoNotFoundException::new);
        Long currentUserId = authService.getCurrentUser().getId();
        if (!video.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("无权发布此视频");
        }
        video.publish();
        return videoConverter.toDto(video);
    }
}
