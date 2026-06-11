package com.plantcare.repository;

import com.plantcare.entity.BrowseHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BrowseHistoryRepository extends JpaRepository<BrowseHistory, Long> {

    Optional<BrowseHistory> findByUserIdAndTargetTypeAndTargetId(Long userId, String targetType, Long targetId);

    List<BrowseHistory> findByUserIdAndTargetTypeOrderByLastViewedAtDesc(Long userId, String targetType);

    List<BrowseHistory> findByUserIdOrderByLastViewedAtDesc(Long userId);

    @Query("SELECT bh.targetId FROM BrowseHistory bh WHERE bh.userId = :userId AND bh.targetType = :targetType ORDER BY bh.viewCount DESC, bh.lastViewedAt DESC")
    List<Long> findTopTargetIdsByUserIdAndTargetType(@Param("userId") Long userId, @Param("targetType") String targetType);

    @Query("SELECT bh.targetId, SUM(bh.viewCount) as totalViews FROM BrowseHistory bh " +
            "WHERE bh.targetType = :targetType " +
            "GROUP BY bh.targetId ORDER BY totalViews DESC")
    List<Object[]> findMostViewedTargetIds(@Param("targetType") String targetType);

    void deleteByUserIdAndTargetTypeAndTargetId(Long userId, String targetType, Long targetId);
}
