package com.franklintju.streamlab.follow;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserFollowRepository extends JpaRepository<UserFollow, UserFollowId> {

    Page<UserFollow> findByFollowerId(Long followerId, Pageable pageable);

    Page<UserFollow> findByFollowingId(Long followingId, Pageable pageable);

    long countByFollowerId(Long followerId);

    long countByFollowingId(Long followingId);
}

