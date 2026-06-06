package com.plantcare.repository;

import com.plantcare.entity.PlantArchive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlantArchiveRepository extends JpaRepository<PlantArchive, Long> {

    List<PlantArchive> findByUserIdOrderByCreatedAtDesc(Long userId);
}
