package com.plantcare.repository;

import com.plantcare.entity.PlantPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlantPhotoRepository extends JpaRepository<PlantPhoto, Long> {

    @Query("SELECT pp FROM PlantPhoto pp WHERE pp.plantArchiveId = :plantArchiveId ORDER BY pp.photoDate DESC, pp.createdAt DESC")
    List<PlantPhoto> findByPlantArchiveIdOrderByPhotoDateDesc(@Param("plantArchiveId") Long plantArchiveId);

    @Query("SELECT pp FROM PlantPhoto pp WHERE pp.plantArchiveId = :plantArchiveId AND pp.isCover = true")
    Optional<PlantPhoto> findCoverByPlantArchiveId(@Param("plantArchiveId") Long plantArchiveId);

    @Modifying
    @Query("UPDATE PlantPhoto pp SET pp.isCover = false WHERE pp.plantArchiveId = :plantArchiveId")
    void clearCoverByPlantArchiveId(@Param("plantArchiveId") Long plantArchiveId);

    @Query("SELECT COUNT(pp) FROM PlantPhoto pp WHERE pp.plantArchiveId = :plantArchiveId")
    Long countByPlantArchiveId(@Param("plantArchiveId") Long plantArchiveId);

    void deleteByPlantArchiveId(Long plantArchiveId);
}
