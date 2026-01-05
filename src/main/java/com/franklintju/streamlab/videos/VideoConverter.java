package com.franklintju.streamlab.videos;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VideoConverter {
    VideoDto toDto(Video video);
    Video toEntity(VideoDto videoDto);
}
