package com.plantcare.repository;

import com.plantcare.entity.FavoriteFolder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteFolderRepository extends JpaRepository<FavoriteFolder, Long> {

    List<FavoriteFolder> findByUserIdOrderBySortOrderAscCreatedAtAsc(Long userId);

    Optional<FavoriteFolder> findByUserIdAndId(Long userId, Long id);

    Optional<FavoriteFolder> findByUserIdAndName(Long userId, String name);

    Optional<FavoriteFolder> findByUserIdAndIsDefaultTrue(Long userId);

    void deleteByUserIdAndId(Long userId, Long id);
}
