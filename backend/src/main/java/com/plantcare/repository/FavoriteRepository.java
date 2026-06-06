package com.plantcare.repository;

import com.plantcare.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    List<Favorite> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Favorite> findByUserIdAndTargetTypeAndTargetId(Long userId, String targetType, Long targetId);

    void deleteByUserIdAndTargetTypeAndTargetId(Long userId, String targetType, Long targetId);
}
