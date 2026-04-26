package com.franklintju.streamlab.users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Optional<Profile> findByUser(User user);
    Optional<Profile> findByUserId(Long userId);
    Optional<Profile> findByUsername(String username);

    @Modifying
    @Query("UPDATE Profile p SET p.followingCount = p.followingCount + :delta WHERE p.user.id = :userId")
    void incrementFollowingCount(@Param("userId") Long userId, @Param("delta") int delta);

    @Modifying
    @Query("UPDATE Profile p SET p.followersCount = p.followersCount + :delta WHERE p.user.id = :userId")
    void incrementFollowersCount(@Param("userId") Long userId, @Param("delta") int delta);
}
