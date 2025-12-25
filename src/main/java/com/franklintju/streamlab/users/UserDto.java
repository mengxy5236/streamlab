package com.franklintju.streamlab.users;

import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String phone;
    private String email;
}
