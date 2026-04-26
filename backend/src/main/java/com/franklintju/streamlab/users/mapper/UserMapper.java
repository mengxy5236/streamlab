package com.franklintju.streamlab.users.mapper;

import com.franklintju.streamlab.videos.VideoDto;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserMapper {
    List<VideoDto> getVideosByUserId(@Param("userId") Long userId);
}
