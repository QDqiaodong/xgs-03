package com.plantcare.repository;

import com.plantcare.entity.PlantCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlantCategoryRepository extends JpaRepository<PlantCategory, Long> {

    List<PlantCategory> findByCategory(String category);

    List<PlantCategory> findByNameContaining(String name);
}
