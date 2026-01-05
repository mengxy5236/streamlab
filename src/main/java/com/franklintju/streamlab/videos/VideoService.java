package com.franklintju.streamlab.videos;

import com.franklintju.streamlab.upload.UploadTask;
import com.franklintju.streamlab.upload.UploadTaskRepository;
import com.franklintju.streamlab.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@RequiredArgsConstructor
@Service
public class VideoService {

    private final UserRepository userRepository;
    private final VideoRepository videoRepository;
    private final VideoConverter videoConverter;
    private final UploadTaskRepository uploadTaskRepository;


    @Transactional
    public VideoDto updateVideo(Long id, updateVideoRequest request) {

        var video = videoRepository.findById(id).orElseThrow(VideoNotFoundException::new);

        // TODO：权限校验（是不是作者本人）

        boolean videoUrlChanged =
                !Objects.equals(video.getVideoUrl(), request.getVideoUrl());

        video.setTitle(request.getTitle());
        video.setDescription(request.getDescription());
        video.setCoverUrl(request.getCoverUrl());
        video.setVideoUrl(request.getVideoUrl());
        video.calcuDuration(video.getVideoUrl());

        if (videoUrlChanged) {
            video.calcuDuration(request.getVideoUrl());
            video.setStatus(Video.VideoStatus.UPLOADING);

            if (!uploadTaskRepository.existsByVideoId(video.getId())) {
                UploadTask task = new UploadTask();
                task.setUser(video.getUser());
                task.setVideo(video);
                uploadTaskRepository.save(task);
            }
        }
        return videoConverter.toDto(video);
    }
}
