package com.plantcare.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {

    private Long userId;

    private Integer experienceLevel;

    private String experienceLevelName;

    private Integer totalCareLogs;

    private Integer totalPlants;

    private Integer consecutiveCareDays;

    private List<String> preferredCategories;

    private Map<String, Double> categoryPreferenceScores;

    private List<String> preferredLightRequirements;

    private List<String> preferredWaterRequirements;

    private Integer avgDifficultyLevel;

    private List<Long> favoritePlantCategoryIds;

    private List<Long> browsedPlantCategoryIds;

    private List<Long> ownedPlantCategoryIds;
}
