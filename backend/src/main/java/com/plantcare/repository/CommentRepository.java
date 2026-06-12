package com.plantcare.repository;

import com.plantcare.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByPostIdOrderByCreatedAtDesc(Long postId);

    List<Comment> findByPostIdOrderByLikeCountDescCreatedAtDesc(Long postId);

    List<Comment> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Comment> findByIsBestAnswerTrue();

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.userId = :userId AND c.isBestAnswer = true")
    Long countBestAnswerByUserId(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(c.likeCount), 0) FROM Comment c WHERE c.userId = :userId")
    Long sumLikeCountByUserId(@Param("userId") Long userId);
}
