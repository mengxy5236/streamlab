package com.franklintju.streamlab.videos;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VideoRepository extends JpaRepository<Video, Long> {
    List<Video> findByUserId(Long id);

    List<Video> findByUserIdAndStatus(Long userId, Video.VideoStatus status);

    Page<Video> findByStatus(Video.VideoStatus status, Pageable pageable);

    @Modifying
    @Query("UPDATE Video v SET v.viewsCount = v.viewsCount + :delta WHERE v.id = :videoId")
    void incrementViews(@Param("videoId") Long videoId, @Param("delta") int delta);

    @Modifying
    @Query("UPDATE Video v SET v.likesCount = v.likesCount + :delta WHERE v.id = :videoId")
    void incrementLikes(@Param("videoId") Long videoId, @Param("delta") int delta);

    @Modifying
    @Query("UPDATE Video v SET v.danmakuCount = v.danmakuCount + :delta WHERE v.id = :videoId")
    void incrementDanmaku(@Param("videoId") Long videoId, @Param("delta") int delta);
}