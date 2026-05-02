package com.franklintju.streamlab.videos.mapper;

import com.franklintju.streamlab.videos.VideoDto;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface VideoMapper {
    List<VideoDto> findVideosByUserId(@Param("userId") Long userId);

    List<VideoDto> findPublicVideosByUserId(@Param("userId") Long userId);

    List<VideoDto> findPublicVideos(@Param("offset") int offset, @Param("size") int size);

    long countPublicVideos();

    boolean existsByUserId(@Param("userId") Long userId);
}
