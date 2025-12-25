package com.franklintju.streamlab.users;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Profile findByUser(User user);
    Profile findByUserId(Long userId);
}