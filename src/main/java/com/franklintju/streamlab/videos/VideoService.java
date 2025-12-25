package com.franklintju.streamlab.videos;

import com.franklintju.streamlab.upload.UploadTask;
import com.franklintju.streamlab.upload.UploadTaskRepository;
import com.franklintju.streamlab.users.UserNotFoundException;
import com.franklintju.streamlab.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class VideoService {

    private final UserRepository userRepository;
    private final VideoRepository videoRepository;
    private final VideoMapper videoMapper;
    private final UploadTaskRepository uploadTaskRepository;

    public List<VideoDto> getVideos(Long userId) {
        var videos = videoRepository.findByUserId(userId);
        if (videos.isEmpty()) {
            if (userRepository.existsById(userId)) {
                throw new VideoNotFoundException();
            } else {
                throw new UserNotFoundException();
            }
        }
        return videos.stream()
                .map(videoMapper::toDto)
                .toList();
    }

    @Transactional
    public VideoDto uploadVideo(Long userId, VideoDto videoDto) {

        var user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        var video = new Video();
        video.setUser(user);
        video.setTitle(videoDto.getTitle());
        video.setDescription(videoDto.getDescription());
        video.setStatus(Video.VideoStatus.UPLOADING);
        videoRepository.save(video);

        var task = new UploadTask();
        task.setUser(user);
        task.setVideo(video);
        uploadTaskRepository.save(task);

        return videoMapper.toDto(video);
    }
}
