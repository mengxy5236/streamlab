package com.franklintju.streamlab.follow.mapper;

import com.franklintju.streamlab.follow.UserSummary;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FollowMapper {

    List<UserSummary> findFollowingByUserId(@Param("userId") Long userId);

    List<UserSummary> findFollowersByUserId(@Param("userId") Long userId);

    void incrementFollowing(@Param("userId") Long userId, @Param("followingId") Long followingId);

    void incrementFollowers(@Param("userId") Long userId, @Param("followerId") Long followerId);

    void decrementFollowing(@Param("userId") Long userId, @Param("followingId") Long followingId);

    void decrementFollowers(@Param("userId") Long userId, @Param("followerId") Long followerId);
}
