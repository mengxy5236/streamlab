package com.franklintju.streamlab.users;

import lombok.Data;

@Data
public class changePasswordRequest {
    private String oldPassword;
    private String newPassword;
}
