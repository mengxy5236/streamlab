package com.franklintju.streamlab.videos;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VideoMapper {
    VideoDto toDto(Video video);
    Video toEntity(VideoDto videoDto);
}
