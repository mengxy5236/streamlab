package com.franklintju.streamlab.upload;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UploadTaskRepository extends JpaRepository<UploadTask, Long> {

    Page<UploadTask> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<UploadTask> findByStatus(UploadTask.TaskStatus status);

    List<UploadTask> findByUserIdAndStatus(Long userId, UploadTask.TaskStatus status);

    long countByUserIdAndStatus(Long userId, UploadTask.TaskStatus status);
}

