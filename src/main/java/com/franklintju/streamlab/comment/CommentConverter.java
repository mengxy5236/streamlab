package com.franklintju.streamlab.comment;

import com.franklintju.streamlab.users.Profile;
import com.franklintju.streamlab.users.ProfileRepository;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;


@Mapper(componentModel = "spring")
public abstract class CommentConverter {

    @Autowired
    protected ProfileRepository profileRepository;

    @Mapping(target = "videoId", source = "video.id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "rootId", source = "root.id")
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "avatarUrl", ignore = true)
    public abstract CommentDto toDto(Comment comment);

    @AfterMapping
    protected void afterMapping(Comment comment, @MappingTarget CommentDto dto) {
        if (profileRepository != null) {
            Profile profile = profileRepository.findByUserId(comment.getUser().getId()).orElse(null);
            if (profile != null) {
                dto.setUsername(profile.getUsername());
                dto.setAvatarUrl(profile.getAvatarUrl());
            }
        }
    }
}
