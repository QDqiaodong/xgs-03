package com.plantcare.repository;

import com.plantcare.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    @Query("SELECT t FROM Tag t WHERE t.userId = :userId ORDER BY t.sortOrder ASC, t.createdAt ASC")
    List<Tag> findByUserIdOrderBySortOrder(@Param("userId") Long userId);

    @Query("SELECT t FROM Tag t WHERE t.userId = :userId AND t.name = :name")
    Optional<Tag> findByUserIdAndName(@Param("userId") Long userId, @Param("name") String name);

    @Query("SELECT t FROM Tag t WHERE t.userId = :userId AND t.id IN :tagIds")
    List<Tag> findByUserIdAndIdIn(@Param("userId") Long userId, @Param("tagIds") List<Long> tagIds);
}
