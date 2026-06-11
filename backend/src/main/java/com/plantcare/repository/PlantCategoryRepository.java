package com.plantcare.repository;

import com.plantcare.entity.PlantCategory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlantCategoryRepository extends JpaRepository<PlantCategory, Long> {

    List<PlantCategory> findByCategory(String category);

    List<PlantCategory> findByNameContaining(String name);

    List<PlantCategory> findByDifficultyLevelLessThanEqualOrderByDifficultyLevelAsc(Integer difficultyLevel);

    List<PlantCategory> findByDifficultyLevel(Integer difficultyLevel);

    List<PlantCategory> findAllByOrderByPopularityScoreDesc();

    @Query("SELECT pc FROM PlantCategory pc WHERE pc.id NOT IN :excludeIds ORDER BY pc.popularityScore DESC")
    List<PlantCategory> findPopularExcludingIds(@Param("excludeIds") List<Long> excludeIds, Pageable pageable);

    @Query("SELECT pc FROM PlantCategory pc WHERE pc.difficultyLevel <= :maxDifficulty AND pc.id NOT IN :excludeIds ORDER BY pc.difficultyLevel ASC, pc.popularityScore DESC")
    List<PlantCategory> findEasyCareExcludingIds(
            @Param("maxDifficulty") Integer maxDifficulty,
            @Param("excludeIds") List<Long> excludeIds,
            Pageable pageable);

    @Query("SELECT pc FROM PlantCategory pc WHERE pc.category IN :categories AND pc.id NOT IN :excludeIds ORDER BY pc.popularityScore DESC")
    List<PlantCategory> findByCategoriesExcludingIds(
            @Param("categories") List<String> categories,
            @Param("excludeIds") List<Long> excludeIds,
            Pageable pageable);

    @Query("SELECT pc.category, COUNT(pc) as cnt FROM PlantCategory pc GROUP BY pc.category ORDER BY cnt DESC")
    List<Object[]> countByCategory();

    @Query("SELECT DISTINCT pc.category FROM PlantCategory pc")
    List<String> findAllCategories();
}
