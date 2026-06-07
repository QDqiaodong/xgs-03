package com.plantcare.controller;

import com.plantcare.entity.PlantCategory;
import com.plantcare.service.PlantCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/plant-categories", produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
public class PlantCategoryController {

    private final PlantCategoryService plantCategoryService;

    @GetMapping
    public ResponseEntity<List<PlantCategory>> getAllCategories() {
        return ResponseEntity.ok(plantCategoryService.getAllCategories());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlantCategory> getCategoryById(@PathVariable Long id) {
        return plantCategoryService.getCategoryById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<PlantCategory>> getCategoriesByCategory(@PathVariable String category) {
        return ResponseEntity.ok(plantCategoryService.getCategoriesByCategory(category));
    }

    @GetMapping("/search")
    public ResponseEntity<List<PlantCategory>> searchCategories(@RequestParam String keyword) {
        return ResponseEntity.ok(plantCategoryService.searchCategories(keyword));
    }
}
