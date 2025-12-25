package com.franklintju.streamlab.videos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VideoStatsRepository extends JpaRepository<VideoStats, Long> {

    @Modifying
    @Query("UPDATE VideoStats s SET s.viewsCount = s.viewsCount + 1 WHERE s.videoId = :videoId")
    void incrementViews(@Param("videoId") Long videoId);

    @Modifying
    @Query("UPDATE VideoStats s SET s.likesCount = s.likesCount + :delta WHERE s.videoId = :videoId")
    void updateLikesCount(@Param("videoId") Long videoId, @Param("delta") int delta);

    @Modifying
    @Query("UPDATE VideoStats s SET s.coinsCount = s.coinsCount + :amount WHERE s.videoId = :videoId")
    void incrementCoins(@Param("videoId") Long videoId, @Param("amount") int amount);

    @Modifying
    @Query("UPDATE VideoStats s SET s.favoritesCount = s.favoritesCount + :delta WHERE s.videoId = :videoId")
    void updateFavoritesCount(@Param("videoId") Long videoId, @Param("delta") int delta);

    @Modifying
    @Query("UPDATE VideoStats s SET s.commentsCount = s.commentsCount + :delta WHERE s.videoId = :videoId")
    void updateCommentsCount(@Param("videoId") Long videoId, @Param("delta") int delta);

    @Modifying
    @Query("UPDATE VideoStats s SET s.sharesCount = s.sharesCount + 1 WHERE s.videoId = :videoId")
    void incrementShares(@Param("videoId") Long videoId);

    @Modifying
    @Query("UPDATE VideoStats s SET s.danmakuCount = s.danmakuCount + :delta WHERE s.videoId = :videoId")
    void updateDanmakuCount(@Param("videoId") Long videoId, @Param("delta") int delta);
}

