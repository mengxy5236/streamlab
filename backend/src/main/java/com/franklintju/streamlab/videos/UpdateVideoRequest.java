package com.franklintju.streamlab.videos;

import lombok.Data;

@Data
public class UpdateVideoRequest {

    private String title;
    private String description;
    private String coverUrl;
    private String videoUrl;

}
