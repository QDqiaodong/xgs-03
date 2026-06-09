package com.plantcare.service;

import com.plantcare.entity.Comment;
import com.plantcare.entity.CommentLike;
import com.plantcare.repository.CommentLikeRepository;
import com.plantcare.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentLikeService {

    private final CommentLikeRepository commentLikeRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public Comment likeComment(Long userId, Long commentId) {
        Optional<Comment> commentOpt = commentRepository.findById(commentId);
        if (commentOpt.isEmpty()) {
            return null;
        }

        if (commentLikeRepository.existsByUserIdAndCommentId(userId, commentId)) {
            return commentOpt.get();
        }

        CommentLike commentLike = new CommentLike();
        commentLike.setUserId(userId);
        commentLike.setCommentId(commentId);
        commentLikeRepository.save(commentLike);

        Comment comment = commentOpt.get();
        comment.setLikeCount(comment.getLikeCount() + 1);
        return commentRepository.save(comment);
    }

    @Transactional
    public Comment unlikeComment(Long userId, Long commentId) {
        Optional<Comment> commentOpt = commentRepository.findById(commentId);
        if (commentOpt.isEmpty()) {
            return null;
        }

        if (!commentLikeRepository.existsByUserIdAndCommentId(userId, commentId)) {
            return commentOpt.get();
        }

        commentLikeRepository.deleteByUserIdAndCommentId(userId, commentId);

        Comment comment = commentOpt.get();
        comment.setLikeCount(Math.max(0, comment.getLikeCount() - 1));
        return commentRepository.save(comment);
    }

    @Transactional
    public Comment toggleLike(Long userId, Long commentId) {
        if (commentLikeRepository.existsByUserIdAndCommentId(userId, commentId)) {
            return unlikeComment(userId, commentId);
        } else {
            return likeComment(userId, commentId);
        }
    }

    public boolean isLiked(Long userId, Long commentId) {
        return commentLikeRepository.existsByUserIdAndCommentId(userId, commentId);
    }

    public long getLikeCount(Long commentId) {
        return commentLikeRepository.countByCommentId(commentId);
    }
}
