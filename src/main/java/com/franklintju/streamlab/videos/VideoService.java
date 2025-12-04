package com.franklintju.streamlab.videos;

import com.franklintju.streamlab.users.UserNotFoundException;
import com.franklintju.streamlab.users.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class VideoService {
    private UserRepository userRepository;
    private VideoRepository videoRepository;
    private VideoMapper videoMapper;

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

    public VideoDto uploadVideo(Long userId, VideoDto videoDto) {
        var user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        var video = Video.createNew(user, videoDto);

        videoRepository.save(video);
        return videoMapper.toDto(video);
    }
}
