package com.plantcare.repository;

import com.plantcare.entity.PlantArchiveTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlantArchiveTagRepository extends JpaRepository<PlantArchiveTag, Long> {

    @Query("SELECT pat FROM PlantArchiveTag pat JOIN FETCH pat.tag WHERE pat.plantArchiveId = :plantArchiveId")
    List<PlantArchiveTag> findByPlantArchiveId(@Param("plantArchiveId") Long plantArchiveId);

    @Query("SELECT pat FROM PlantArchiveTag pat JOIN FETCH pat.tag WHERE pat.plantArchiveId IN :plantArchiveIds")
    List<PlantArchiveTag> findByPlantArchiveIdIn(@Param("plantArchiveIds") List<Long> plantArchiveIds);

    @Query("SELECT pat FROM PlantArchiveTag pat WHERE pat.plantArchiveId = :plantArchiveId AND pat.tagId = :tagId")
    Optional<PlantArchiveTag> findByPlantArchiveIdAndTagId(@Param("plantArchiveId") Long plantArchiveId, @Param("tagId") Long tagId);

    @Query("SELECT DISTINCT pat.plantArchiveId FROM PlantArchiveTag pat WHERE pat.tagId = :tagId")
    List<Long> findPlantArchiveIdsByTagId(@Param("tagId") Long tagId);

    @Query("SELECT DISTINCT pat.plantArchiveId FROM PlantArchiveTag pat WHERE pat.tagId IN :tagIds")
    List<Long> findPlantArchiveIdsByTagIdIn(@Param("tagIds") List<Long> tagIds);

    @Query("SELECT COUNT(pat) FROM PlantArchiveTag pat WHERE pat.tagId = :tagId")
    Long countByTagId(@Param("tagId") Long tagId);

    void deleteByPlantArchiveIdAndTagId(Long plantArchiveId, Long tagId);

    void deleteByPlantArchiveId(Long plantArchiveId);
}
