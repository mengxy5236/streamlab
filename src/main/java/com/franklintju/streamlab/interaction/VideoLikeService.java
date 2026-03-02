package com.franklintju.streamlab.interaction;

import com.franklintju.streamlab.exceptions.AlreadyLikedException;
import com.franklintju.streamlab.exceptions.NotLikedException;
import com.franklintju.streamlab.exceptions.VideoNotLikeableException;
import com.franklintju.streamlab.users.User;
import com.franklintju.streamlab.users.UserRepository;
import com.franklintju.streamlab.videos.Video;
import com.franklintju.streamlab.videos.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VideoLikeService {

    private final VideoLikeRepository likeRepository;
    private final VideoRepository videoRepository;
    private final UserRepository userRepository;

    @Transactional
    public void like(Long userId, Long videoId) {
        if (likeRepository.existsById(new VideoLikeId(userId, videoId))) {
            throw new AlreadyLikedException();
        }
        User user = userRepository.findById(userId).orElseThrow();
        Video video = videoRepository.findById(videoId).orElseThrow();

        if (!video.isLikeable()) {
            throw new VideoNotLikeableException("视频无法点赞");
        }

        VideoLike like = new VideoLike();
        like.setId(new VideoLikeId(userId, videoId));
        like.setUser(user);
        like.setVideo(video);
        likeRepository.save(like);
        video.like();
    }

    @Transactional
    public void unlike(Long userId, Long videoId) {
        if (!likeRepository.existsById(new VideoLikeId(userId, videoId))) {
            throw new NotLikedException();
        }
        likeRepository.deleteById(new VideoLikeId(userId, videoId));
        videoRepository.findById(videoId).ifPresent(Video::unlike);
    }

    public boolean hasLiked(Long userId, Long videoId) {
        return likeRepository.existsById(new VideoLikeId(userId, videoId));
    }

    public long getLikesCount(Long videoId) {
        return likeRepository.countByIdVideoId(videoId);
    }

    public Page<VideoLike> getUserLikes(Long userId, Pageable pageable) {
        return likeRepository.findByIdUserId(userId, pageable);
    }
}
