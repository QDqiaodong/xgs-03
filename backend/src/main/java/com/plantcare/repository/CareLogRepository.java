package com.plantcare.repository;

import com.plantcare.entity.CareLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CareLogRepository extends JpaRepository<CareLog, Long> {

    List<CareLog> findByPlantArchiveIdOrderByLogDateDesc(Long plantArchiveId);

    List<CareLog> findByUserIdOrderByLogDateDesc(Long userId);
}
