package com.franklintju.streamlab.videos;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VideoRepository extends JpaRepository<Video, Long> {
    List<Video> findByUserId(Long id);

    List<Video> findByUserIdAndStatus(Long userId, Video.VideoStatus status);

    Page<Video> findByStatus(Video.VideoStatus status, Pageable pageable);
}