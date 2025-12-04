package com.franklintju.streamlab.interaction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoLikeRepository extends JpaRepository<VideoLike, VideoLikeId> {

    Page<VideoLike> findByIdUserId(Long userId, Pageable pageable);

    long countByIdVideoId(Long videoId);
}

