package com.franklintju.streamlab.comment;

import com.franklintju.streamlab.users.User;
import com.franklintju.streamlab.users.UserRepository;
import com.franklintju.streamlab.videos.Video;
import com.franklintju.streamlab.videos.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final VideoRepository videoRepository;
    private final UserRepository userRepository;

    @Transactional
    public Comment createComment(Long userId, Long videoId, String content, Long parentId, Long rootId) {
        User user = userRepository.findById(userId).orElseThrow();
        Video video = videoRepository.findById(videoId).orElseThrow();

        Comment comment = new Comment();
        comment.setUser(user);
        comment.setVideo(video);
        comment.setContent(content);

        if (parentId != null) {
            Comment parent = commentRepository.findById(parentId).orElseThrow();
            comment.setParent(parent);
            if (rootId != null) {
                Comment root = commentRepository.findById(rootId).orElseThrow();
                comment.setRoot(root);
            } else {
                comment.setRoot(parent);
            }
            commentRepository.updateReplyCount(parentId, 1);
        }

        return commentRepository.save(comment);
    }

    @Transactional
    public Comment updateComment(Long commentId, String content) {
        Comment comment = commentRepository.findById(commentId).orElseThrow();
        comment.setContent(content);
        return commentRepository.save(comment);
    }

    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow();
        if (comment.getParent() != null) {
            commentRepository.updateReplyCount(comment.getParent().getId(), -1);
        }
        commentRepository.delete(comment);
    }

    public Page<Comment> getVideoComments(Long videoId, Pageable pageable) {
        return commentRepository.findByVideoIdAndRootIsNullAndStatus(videoId, Comment.CommentStatus.VISIBLE, pageable);
    }

    public Page<Comment> getReplies(Long rootId, Pageable pageable) {
        return commentRepository.findByRootIdAndStatus(rootId, Comment.CommentStatus.VISIBLE, pageable);
    }

    public Page<Comment> getUserComments(Long userId, Pageable pageable) {
        return commentRepository.findByUserId(userId, pageable);
    }

    public long getCommentCount(Long videoId) {
        return commentRepository.countByVideoIdAndStatus(videoId, Comment.CommentStatus.VISIBLE);
    }

    @Transactional
    public void likeComment(Long commentId) {
        commentRepository.updateLikesCount(commentId, 1);
    }

    @Transactional
    public void unlikeComment(Long commentId) {
        commentRepository.updateLikesCount(commentId, -1);
    }
}
