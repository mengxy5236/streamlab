package com.franklintju.streamlab.interaction;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class VideoFavoriteId implements Serializable {

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "video_id")
    private Long videoId;
}

