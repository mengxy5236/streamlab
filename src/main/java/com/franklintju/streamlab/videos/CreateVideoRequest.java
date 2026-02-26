package com.franklintju.streamlab.videos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateVideoRequest {
    @NotBlank(message = "标题不能为空")
    private String title;

    private String description;
    private String coverUrl;
}
