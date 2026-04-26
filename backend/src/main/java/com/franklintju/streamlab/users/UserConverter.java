package com.franklintju.streamlab.users;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserConverter {
    User toEntity(RegisterUserRequest registerRequest);
    UserDto toDto(User user);
}
