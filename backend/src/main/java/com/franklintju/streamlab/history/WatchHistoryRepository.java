package com.franklintju.streamlab.history;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WatchHistoryRepository extends JpaRepository<WatchHistory, Long> {

    Page<WatchHistory> findByUserIdOrderByWatchedAtDesc(Long userId, Pageable pageable);

    Optional<WatchHistory> findByUserIdAndVideoId(Long userId, Long videoId);

    void deleteByUserIdAndVideoId(Long userId, Long videoId);

    void deleteByUserId(Long userId);
}

