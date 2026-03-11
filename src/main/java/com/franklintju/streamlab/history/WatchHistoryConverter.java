package com.franklintju.streamlab.history;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public abstract class WatchHistoryConverter {

    @Mapping(target = "videoId", ignore = true)
    @Mapping(target = "videoTitle", ignore = true)
    @Mapping(target = "videoCoverUrl", ignore = true)
    public abstract WatchHistoryDto toDto(WatchHistory history);

    @AfterMapping
    protected void afterMapping(WatchHistory history, @MappingTarget WatchHistoryDto dto) {
        dto.setVideoId(history.getVideo().getId());
        dto.setVideoTitle(history.getVideo().getTitle());
        dto.setVideoCoverUrl(history.getVideo().getCoverUrl());
    }
}
