package com.plantcare.repository;

import com.plantcare.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    List<Favorite> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Favorite> findByUserIdAndFolderIdOrderByCreatedAtDesc(Long userId, Long folderId);

    List<Favorite> findByUserIdAndFolderIdIsNullOrderByCreatedAtDesc(Long userId);

    Optional<Favorite> findByUserIdAndTargetTypeAndTargetId(Long userId, String targetType, Long targetId);

    Optional<Favorite> findByUserIdAndTargetTypeAndTargetIdAndFolderId(Long userId, String targetType, Long targetId, Long folderId);

    void deleteByUserIdAndTargetTypeAndTargetId(Long userId, String targetType, Long targetId);

    void deleteByUserIdAndFolderId(Long userId, Long folderId);

    long countByUserIdAndFolderId(Long userId, Long folderId);

    long countByUserIdAndFolderIdIsNull(Long userId);
}
