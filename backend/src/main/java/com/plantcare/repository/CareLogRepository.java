package com.plantcare.repository;

import com.plantcare.entity.CareLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CareLogRepository extends JpaRepository<CareLog, Long>, JpaSpecificationExecutor<CareLog> {

    List<CareLog> findByPlantArchiveIdOrderByLogDateDescCreatedAtDesc(Long plantArchiveId);

    List<CareLog> findByUserIdOrderByLogDateDesc(Long userId);

    Page<CareLog> findByPlantArchiveIdOrderByLogDateDescCreatedAtDesc(Long plantArchiveId, Pageable pageable);
}
