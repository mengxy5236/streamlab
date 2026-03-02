package com.franklintju.streamlab.history;

import com.franklintju.streamlab.users.User;
import com.franklintju.streamlab.users.UserRepository;
import com.franklintju.streamlab.videos.Video;
import com.franklintju.streamlab.videos.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WatchHistoryService {

    private final WatchHistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final VideoRepository videoRepository;

    @Transactional
    public WatchHistory recordProgress(Long userId, Long videoId, Integer progress, Integer duration) {
        User user = userRepository.findById(userId).orElseThrow();
        Video video = videoRepository.findById(videoId).orElseThrow();

        Optional<WatchHistory> existing = historyRepository.findByUserIdAndVideoId(userId, videoId);

        WatchHistory history;
        if (existing.isPresent()) {
            history = existing.get();
        } else {
            history = new WatchHistory();
            history.setUser(user);
            history.setVideo(video);
        }

        history.setProgress(progress);
        history.setDuration(duration);
        return historyRepository.save(history);
    }

    public Page<WatchHistory> getUserHistory(Long userId, Pageable pageable) {
        return historyRepository.findByUserIdOrderByWatchedAtDesc(userId, pageable);
    }

    @Transactional
    public void deleteHistory(Long userId, Long videoId) {
        historyRepository.deleteByUserIdAndVideoId(userId, videoId);
    }

    @Transactional
    public void clearHistory(Long userId) {
        historyRepository.deleteByUserId(userId);
    }
}
