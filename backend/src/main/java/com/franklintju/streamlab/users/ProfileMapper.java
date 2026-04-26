package com.franklintju.streamlab.users;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProfileMapper {
    ToFullDto toFullDto(Profile profile);
    ToPublicDto toPublicDto(Profile profile);
    ToGuestDto toGuestDto(Profile profile);
}
