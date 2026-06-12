package com.plantcare.repository;

import com.plantcare.entity.CareCheckIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CareCheckInRepository extends JpaRepository<CareCheckIn, Long> {

    Optional<CareCheckIn> findByUserIdAndCheckinDate(Long userId, LocalDate checkinDate);

    List<CareCheckIn> findByUserIdOrderByCheckinDateDesc(Long userId);

    List<CareCheckIn> findByUserIdAndCheckinDateBetweenOrderByCheckinDateAsc(Long userId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT c.checkinDate FROM CareCheckIn c WHERE c.userId = :userId ORDER BY c.checkinDate DESC")
    List<LocalDate> findAllCheckinDatesByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(c) FROM CareCheckIn c WHERE c.userId = :userId")
    Long countByUserId(@Param("userId") Long userId);

    boolean existsByUserIdAndCheckinDate(Long userId, LocalDate checkinDate);
}
