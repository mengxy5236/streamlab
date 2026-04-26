package com.franklintju.streamlab.category;

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
public class VideoCategoryId implements Serializable {

    @Column(name = "video_id")
    private Long videoId;

    @Column(name = "category_id")
    private Long categoryId;
}

