package com.plantcare.service;

import com.plantcare.entity.PlantCategory;
import com.plantcare.repository.PlantCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PlantCategoryService {

    private final PlantCategoryRepository plantCategoryRepository;

    @Cacheable(value = "plantCategories", key = "'all'")
    public List<PlantCategory> getAllCategories() {
        return plantCategoryRepository.findAll();
    }

    @Cacheable(value = "plantCategories", key = "#id")
    public Optional<PlantCategory> getCategoryById(Long id) {
        return plantCategoryRepository.findById(id);
    }

    @Cacheable(value = "plantCategories", key = "'category:' + #category")
    public List<PlantCategory> getCategoriesByCategory(String category) {
        return plantCategoryRepository.findByCategory(category);
    }

    public List<PlantCategory> searchCategories(String keyword) {
        return plantCategoryRepository.findByNameContaining(keyword);
    }
}
