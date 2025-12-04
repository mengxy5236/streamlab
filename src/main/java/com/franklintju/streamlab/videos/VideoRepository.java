package com.franklintju.streamlab.videos;

import com.franklintju.streamlab.users.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VideoRepository extends JpaRepository<Video, Long> {
    List<Video> findByUserId(Long id);
}