package com.plantcare.dto;

import com.plantcare.entity.Comment;
import com.plantcare.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentDTO {
    private Long id;
    private Long postId;
    private Long userId;
    private Long parentId;
    private String content;
    private Integer likeCount;
    private Boolean isBestAnswer;
    private User user;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean liked;

    public static CommentDTO fromEntity(Comment comment, boolean liked) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setPostId(comment.getPostId());
        dto.setUserId(comment.getUserId());
        dto.setParentId(comment.getParentId());
        dto.setContent(comment.getContent());
        dto.setLikeCount(comment.getLikeCount());
        dto.setIsBestAnswer(comment.getIsBestAnswer());
        dto.setUser(comment.getUser());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());
        dto.setLiked(liked);
        return dto;
    }
}
