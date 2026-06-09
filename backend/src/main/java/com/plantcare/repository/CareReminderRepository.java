package com.plantcare.repository;

import com.plantcare.entity.CareReminder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CareReminderRepository extends JpaRepository<CareReminder, Long>, JpaSpecificationExecutor<CareReminder> {

    List<CareReminder> findByUserIdOrderByReminderDateDescCreatedAtDesc(Long userId);

    Page<CareReminder> findByUserId(Long userId, Pageable pageable);

    List<CareReminder> findByUserIdAndStatusOrderByReminderDateAscCreatedAtDesc(Long userId, String status);

    Page<CareReminder> findByUserIdAndStatus(Long userId, String status, Pageable pageable);

    List<CareReminder> findByPlantArchiveIdOrderByReminderDateDescCreatedAtDesc(Long plantArchiveId);

    Optional<CareReminder> findByPlantArchiveIdAndReminderTypeAndReminderDate(
            Long plantArchiveId, String reminderType, LocalDate reminderDate);

    @Query("SELECT cr FROM CareReminder cr LEFT JOIN FETCH cr.plantArchive WHERE cr.userId = :userId ORDER BY cr.reminderDate DESC, cr.createdAt DESC")
    List<CareReminder> findByUserIdWithPlantArchive(@Param("userId") Long userId);

    @Query("SELECT cr FROM CareReminder cr LEFT JOIN FETCH cr.plantArchive WHERE cr.userId = :userId AND cr.status = :status ORDER BY cr.reminderDate ASC, cr.createdAt DESC")
    List<CareReminder> findByUserIdAndStatusWithPlantArchive(@Param("userId") Long userId, @Param("status") String status);

    @Query("SELECT cr FROM CareReminder cr LEFT JOIN FETCH cr.plantArchive WHERE cr.id = :id")
    Optional<CareReminder> findByIdWithPlantArchive(@Param("id") Long id);

    boolean existsByPlantArchiveIdAndReminderTypeAndReminderDateAndStatusNot(
            Long plantArchiveId, String reminderType, LocalDate reminderDate, String status);
}
