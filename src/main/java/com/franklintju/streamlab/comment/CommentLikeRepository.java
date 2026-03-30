package com.franklintju.streamlab.comment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentLikeRepository extends JpaRepository<CommentLike, CommentLikeId> {
    Page<CommentLike> findByIdUserId(Long userId, Pageable pageable);
    boolean existsById(CommentLikeId id);
}
