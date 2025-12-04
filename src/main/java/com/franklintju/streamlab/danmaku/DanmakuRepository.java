package com.franklintju.streamlab.danmaku;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface DanmakuRepository extends JpaRepository<Danmaku, Long> {

    List<Danmaku> findByVideoIdOrderBySendTime(Long videoId);

    @Query("SELECT d FROM Danmaku d WHERE d.video.id = :videoId AND d.sendTime BETWEEN :start AND :end ORDER BY d.sendTime")
    List<Danmaku> findByVideoIdAndTimeRange(
            @Param("videoId") Long videoId,
            @Param("start") BigDecimal start,
            @Param("end") BigDecimal end);

    long countByVideoId(Long videoId);

    void deleteByVideoId(Long videoId);
}

