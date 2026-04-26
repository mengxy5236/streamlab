package com.franklintju.streamlab.category;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VideoCategoryRepository extends JpaRepository<VideoCategory, VideoCategoryId> {

    List<VideoCategory> findByIdVideoId(Long videoId);

    List<VideoCategory> findByIdCategoryId(Long categoryId);

    void deleteByIdVideoId(Long videoId);
}

