package com.plantcare.repository;

import com.plantcare.entity.CareLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CareLogRepository extends JpaRepository<CareLog, Long>, JpaSpecificationExecutor<CareLog> {

    List<CareLog> findByPlantArchiveIdOrderByLogDateDescCreatedAtDesc(Long plantArchiveId);

    List<CareLog> findByUserIdOrderByLogDateDesc(Long userId);

    Page<CareLog> findByPlantArchiveIdOrderByLogDateDescCreatedAtDesc(Long plantArchiveId, Pageable pageable);

    CareLog findTopByPlantArchiveIdAndOperationTypeOrderByLogDateDescCreatedAtDesc(Long plantArchiveId, String operationType);

    @Query("SELECT cl.logDate FROM CareLog cl WHERE cl.userId = :userId AND cl.logDate BETWEEN :startDate AND :endDate ORDER BY cl.logDate ASC")
    List<LocalDate> findDistinctLogDatesByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT cl.operationType, COUNT(cl) FROM CareLog cl WHERE cl.userId = :userId AND cl.logDate BETWEEN :startDate AND :endDate GROUP BY cl.operationType")
    List<Object[]> countByOperationTypeAndUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT cl.plantArchiveId, COUNT(cl) as cnt FROM CareLog cl WHERE cl.userId = :userId AND cl.logDate BETWEEN :startDate AND :endDate GROUP BY cl.plantArchiveId ORDER BY cnt DESC")
    List<Object[]> countByPlantArchiveIdAndUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT FUNCTION('DATE_FORMAT', cl.logDate, '%Y-%m') as month, COUNT(cl) FROM CareLog cl WHERE cl.userId = :userId AND cl.logDate BETWEEN :startDate AND :endDate GROUP BY month ORDER BY month ASC")
    List<Object[]> countByMonthAndUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT pc.id, pc.name, COUNT(cl) FROM CareLog cl " +
            "JOIN PlantArchive pa ON cl.plantArchiveId = pa.id " +
            "LEFT JOIN PlantCategory pc ON pa.plantCategoryId = pc.id " +
            "WHERE cl.userId = :userId AND cl.logDate BETWEEN :startDate AND :endDate " +
            "GROUP BY pc.id, pc.name ORDER BY COUNT(cl) DESC")
    List<Object[]> countByCategoryAndUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(cl) FROM CareLog cl WHERE cl.userId = :userId AND cl.logDate BETWEEN :startDate AND :endDate")
    Long countByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(cl) FROM CareLog cl WHERE cl.userId = :userId")
    Long countByUserId(@Param("userId") Long userId);
}
