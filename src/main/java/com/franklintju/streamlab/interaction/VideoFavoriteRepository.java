package com.franklintju.streamlab.interaction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoFavoriteRepository extends JpaRepository<VideoFavorite, VideoFavoriteId> {

    Page<VideoFavorite> findByIdUserId(Long userId, Pageable pageable);

    long countByIdVideoId(Long videoId);
}

