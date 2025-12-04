package com.franklintju.streamlab.comment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findByVideoIdAndRootIsNullAndStatus(Long videoId, Comment.CommentStatus status, Pageable pageable);

    Page<Comment> findByRootIdAndStatus(Long rootId, Comment.CommentStatus status, Pageable pageable);

    Page<Comment> findByUserId(Long userId, Pageable pageable);

    long countByVideoIdAndStatus(Long videoId, Comment.CommentStatus status);

    @Modifying
    @Query("UPDATE Comment c SET c.likesCount = c.likesCount + :delta WHERE c.id = :commentId")
    void updateLikesCount(@Param("commentId") Long commentId, @Param("delta") int delta);

    @Modifying
    @Query("UPDATE Comment c SET c.replyCount = c.replyCount + :delta WHERE c.id = :commentId")
    void updateReplyCount(@Param("commentId") Long commentId, @Param("delta") int delta);
}

