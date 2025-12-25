package com.franklintju.streamlab.follow;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class FollowRequest {

    @NotNull(message = "FollowerId 不能为空！")
    @Positive(message = "positive!")
    Long followerId;

    @NotNull(message = "FollowingId 不能为空！")
    @Positive(message = "positive!")
    Long followingId;

}
