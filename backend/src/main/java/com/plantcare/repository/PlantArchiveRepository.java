package com.plantcare.repository;

import com.plantcare.entity.PlantArchive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlantArchiveRepository extends JpaRepository<PlantArchive, Long> {

    @Query("SELECT pa FROM PlantArchive pa LEFT JOIN FETCH pa.plantCategory WHERE pa.userId = :userId ORDER BY pa.createdAt DESC")
    List<PlantArchive> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    @Query("SELECT pa FROM PlantArchive pa LEFT JOIN FETCH pa.plantCategory WHERE pa.id = :id")
    Optional<PlantArchive> findById(@Param("id") Long id);

    @Query("SELECT DISTINCT pa.plantCategoryId FROM PlantArchive pa WHERE pa.userId = :userId AND pa.plantCategoryId IS NOT NULL")
    List<Long> findDistinctPlantCategoryIdsByUserId(@Param("userId") Long userId);

    @Query("SELECT pa.plantCategoryId, COUNT(pa) as cnt FROM PlantArchive pa " +
            "WHERE pa.userId = :userId AND pa.plantCategoryId IS NOT NULL " +
            "GROUP BY pa.plantCategoryId ORDER BY cnt DESC")
    List<Object[]> countByPlantCategoryIdAndUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(pa) FROM PlantArchive pa WHERE pa.userId = :userId")
    Long countByUserId(@Param("userId") Long userId);

    @Query("SELECT pc.category, COUNT(pa) as cnt FROM PlantArchive pa " +
            "LEFT JOIN PlantCategory pc ON pa.plantCategoryId = pc.id " +
            "WHERE pa.userId = :userId " +
            "GROUP BY pc.category ORDER BY cnt DESC")
    List<Object[]> countByCategoryAndUserId(@Param("userId") Long userId);

    @Query("SELECT pc.lightRequirement, COUNT(pa) as cnt FROM PlantArchive pa " +
            "LEFT JOIN PlantCategory pc ON pa.plantCategoryId = pc.id " +
            "WHERE pa.userId = :userId AND pc.lightRequirement IS NOT NULL " +
            "GROUP BY pc.lightRequirement ORDER BY cnt DESC")
    List<Object[]> countByLightRequirementAndUserId(@Param("userId") Long userId);

    @Query("SELECT pc.waterRequirement, COUNT(pa) as cnt FROM PlantArchive pa " +
            "LEFT JOIN PlantCategory pc ON pa.plantCategoryId = pc.id " +
            "WHERE pa.userId = :userId AND pc.waterRequirement IS NOT NULL " +
            "GROUP BY pc.waterRequirement ORDER BY cnt DESC")
    List<Object[]> countByWaterRequirementAndUserId(@Param("userId") Long userId);

    @Query("SELECT AVG(pc.difficultyLevel) FROM PlantArchive pa " +
            "LEFT JOIN PlantCategory pc ON pa.plantCategoryId = pc.id " +
            "WHERE pa.userId = :userId AND pc.difficultyLevel IS NOT NULL")
    Double findAvgDifficultyLevelByUserId(@Param("userId") Long userId);

    @Query("SELECT DISTINCT u.id FROM PlantArchive pa " +
            "JOIN User u ON pa.userId = u.id " +
            "WHERE pa.plantCategoryId IN :categoryIds AND u.id != :excludeUserId")
    List<Long> findUserIdsWithSamePlantCategories(
            @Param("categoryIds") List<Long> categoryIds,
            @Param("excludeUserId") Long excludeUserId);
}
