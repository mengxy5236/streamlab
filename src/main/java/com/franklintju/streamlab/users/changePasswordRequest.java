package com.franklintju.streamlab.users;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class changePasswordRequest {
    @NotBlank(message = "Password is required")
    private String oldPassword;
    @NotBlank(message = "Password is required")
    private String newPassword;
}
