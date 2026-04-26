package com.franklintju.streamlab.users;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ToFullDto implements ProfileDto {
    private String username;
    private String avatarUrl;
    private String bio;
    private Profile.Gender gender;
    private LocalDate birthday;
    private Integer level;
    private Integer coins;
    private Integer followersCount;
    private Integer followingCount;
}
