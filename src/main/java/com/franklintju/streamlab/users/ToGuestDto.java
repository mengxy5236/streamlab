package com.franklintju.streamlab.users;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ToGuestDto implements ProfileDto {
    private String username;
    private String avatarUrl;
    private Profile.Gender gender;
    private Integer level;
}
