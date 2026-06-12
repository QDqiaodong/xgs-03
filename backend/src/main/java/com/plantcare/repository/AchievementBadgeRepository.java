package com.plantcare.repository;

import com.plantcare.entity.AchievementBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AchievementBadgeRepository extends JpaRepository<AchievementBadge, Long> {

    List<AchievementBadge> findByUserIdOrderByStreakDaysAsc(Long userId);

    Optional<AchievementBadge> findByUserIdAndBadgeType(Long userId, String badgeType);

    boolean existsByUserIdAndBadgeType(Long userId, String badgeType);
}
