package com.franklintju.streamlab.users;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ToPublicDto implements ProfileDto {
    private String username;
    private String avatarUrl;
    private String bio;
    private Profile.Gender gender;
    private LocalDate birthday;
    private Integer level;
}
