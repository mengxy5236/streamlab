package com.franklintju.streamlab.interaction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VideoCoinRepository extends JpaRepository<VideoCoin, Long> {

    Page<VideoCoin> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM VideoCoin c WHERE c.user.id = :userId AND c.video.id = :videoId")
    int sumAmountByUserAndVideo(@Param("userId") Long userId, @Param("videoId") Long videoId);

    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM VideoCoin c WHERE c.video.id = :videoId")
    long sumAmountByVideoId(@Param("videoId") Long videoId);
}

