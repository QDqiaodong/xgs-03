package com.plantcare.service;

import com.plantcare.dto.CommentDTO;
import com.plantcare.entity.Comment;
import com.plantcare.entity.Post;
import com.plantcare.repository.CommentRepository;
import com.plantcare.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentLikeService commentLikeService;
    private final PostRepository postRepository;
    private final PointService pointService;

    public List<Comment> getPostComments(Long postId) {
        return commentRepository.findByPostIdOrderByIsBestAnswerDescCreatedAtDesc(postId);
    }

    public List<Comment> getPostComments(Long postId, String sortBy) {
        if ("likes".equalsIgnoreCase(sortBy)) {
            return commentRepository.findByPostIdOrderByIsBestAnswerDescLikeCountDescCreatedAtDesc(postId);
        }
        return commentRepository.findByPostIdOrderByIsBestAnswerDescCreatedAtDesc(postId);
    }

    public List<CommentDTO> getPostCommentsWithLikeStatus(Long postId, Long userId, String sortBy) {
        List<Comment> comments = getPostComments(postId, sortBy);
        return comments.stream()
                .map(comment -> CommentDTO.fromEntity(comment, commentLikeService.isLiked(userId, comment.getId())))
                .collect(Collectors.toList());
    }

    public List<Comment> getUserComments(Long userId) {
        return commentRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Optional<Comment> getCommentById(Long id) {
        return commentRepository.findById(id);
    }

    public Comment createComment(Comment comment) {
        return commentRepository.save(comment);
    }

    public Comment updateComment(Long id, Comment comment) {
        comment.setId(id);
        return commentRepository.save(comment);
    }

    public void deleteComment(Long id) {
        commentRepository.deleteById(id);
    }

    @Transactional
    public Comment setBestAnswer(Long commentId, Long operatorUserId) {
        Optional<Comment> commentOpt = commentRepository.findById(commentId);
        if (commentOpt.isEmpty()) {
            return null;
        }
        Comment comment = commentOpt.get();
        Long postId = comment.getPostId();

        Optional<Post> postOpt = postRepository.findById(postId);
        if (postOpt.isEmpty()) {
            return null;
        }
        Post post = postOpt.get();

        if (!post.getUserId().equals(operatorUserId)) {
            throw new RuntimeException("只有帖子作者才能设置最佳答案");
        }

        if (comment.getUserId().equals(post.getUserId())) {
            throw new RuntimeException("不能采纳自己的评论为最佳答案");
        }

        if (Boolean.TRUE.equals(comment.getIsBestAnswer())) {
            return comment;
        }

        List<Comment> postComments = commentRepository.findByPostIdOrderByIsBestAnswerDescCreatedAtDesc(postId);
        for (Comment c : postComments) {
            if (Boolean.TRUE.equals(c.getIsBestAnswer()) && !c.getId().equals(commentId)) {
                c.setIsBestAnswer(false);
                commentRepository.save(c);
                pointService.deductPoints(c.getUserId(), PointService.BEST_ANSWER_REWARD,
                        "最佳答案被取消", "comment", c.getId());
            }
        }

        comment.setIsBestAnswer(true);
        Comment savedComment = commentRepository.save(comment);

        post.setIsResolved(true);
        postRepository.save(post);

        pointService.addPoints(comment.getUserId(), PointService.BEST_ANSWER_REWARD,
                "回答被采纳为最佳答案", "comment", commentId);

        return savedComment;
    }

    @Transactional
    public Comment cancelBestAnswer(Long commentId, Long operatorUserId) {
        Optional<Comment> commentOpt = commentRepository.findById(commentId);
        if (commentOpt.isEmpty()) {
            return null;
        }
        Comment comment = commentOpt.get();
        Long postId = comment.getPostId();

        Optional<Post> postOpt = postRepository.findById(postId);
        if (postOpt.isEmpty()) {
            return null;
        }
        Post post = postOpt.get();

        if (!post.getUserId().equals(operatorUserId)) {
            throw new RuntimeException("只有帖子作者才能取消最佳答案");
        }

        if (!Boolean.TRUE.equals(comment.getIsBestAnswer())) {
            return comment;
        }

        comment.setIsBestAnswer(false);
        Comment savedComment = commentRepository.save(comment);

        boolean hasOtherBestAnswer = commentRepository.findByPostIdOrderByIsBestAnswerDescCreatedAtDesc(postId)
                .stream()
                .anyMatch(c -> Boolean.TRUE.equals(c.getIsBestAnswer()));
        if (!hasOtherBestAnswer) {
            post.setIsResolved(false);
            postRepository.save(post);
        }

        pointService.deductPoints(comment.getUserId(), PointService.BEST_ANSWER_REWARD,
                "最佳答案被取消", "comment", commentId);

        return savedComment;
    }

    @Deprecated
    public Comment setBestAnswer(Long commentId, boolean isBest) {
        return null;
    }
}
