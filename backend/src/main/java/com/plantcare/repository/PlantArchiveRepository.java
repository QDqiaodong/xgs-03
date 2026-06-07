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
}
