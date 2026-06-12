package com.plantcare.repository;

import com.plantcare.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findByPostTypeOrderByCreatedAtDesc(String postType, Pageable pageable);

    List<Post> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT p FROM Post p WHERE p.postType = :postType AND p.plantCategoryId = :plantCategoryId ORDER BY p.createdAt DESC")
    Page<Post> findByPostTypeAndPlantCategoryIdOrderByCreatedAtDesc(
            @Param("postType") String postType,
            @Param("plantCategoryId") Long plantCategoryId,
            Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.postType = :postType AND (p.title LIKE %:keyword% OR p.content LIKE %:keyword%) ORDER BY p.createdAt DESC")
    Page<Post> findByPostTypeAndKeywordOrderByCreatedAtDesc(
            @Param("postType") String postType,
            @Param("keyword") String keyword,
            Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.plantCategoryId = :plantCategoryId ORDER BY p.createdAt DESC")
    Page<Post> findByPlantCategoryIdOrderByCreatedAtDesc(
            @Param("plantCategoryId") Long plantCategoryId,
            Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.title LIKE %:keyword% OR p.content LIKE %:keyword% ORDER BY p.createdAt DESC")
    Page<Post> findByKeywordOrderByCreatedAtDesc(
            @Param("keyword") String keyword,
            Pageable pageable);

    Page<Post> findAllByOrderByHotnessScoreDesc(Pageable pageable);

    Page<Post> findByPostTypeOrderByHotnessScoreDesc(String postType, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.postType = :postType AND p.plantCategoryId = :plantCategoryId ORDER BY p.hotnessScore DESC")
    Page<Post> findByPostTypeAndPlantCategoryIdOrderByHotnessScoreDesc(
            @Param("postType") String postType,
            @Param("plantCategoryId") Long plantCategoryId,
            Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.postType = :postType AND (p.title LIKE %:keyword% OR p.content LIKE %:keyword%) ORDER BY p.hotnessScore DESC")
    Page<Post> findByPostTypeAndKeywordOrderByHotnessScoreDesc(
            @Param("postType") String postType,
            @Param("keyword") String keyword,
            Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.plantCategoryId = :plantCategoryId ORDER BY p.hotnessScore DESC")
    Page<Post> findByPlantCategoryIdOrderByHotnessScoreDesc(
            @Param("plantCategoryId") Long plantCategoryId,
            Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.title LIKE %:keyword% OR p.content LIKE %:keyword% ORDER BY p.hotnessScore DESC")
    Page<Post> findByKeywordOrderByHotnessScoreDesc(
            @Param("keyword") String keyword,
            Pageable pageable);

    @Query("SELECT COUNT(p) FROM Post p WHERE p.userId = :userId")
    Long countByUserId(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(p.likeCount), 0) FROM Post p WHERE p.userId = :userId")
    Long sumLikeCountByUserId(@Param("userId") Long userId);
}
