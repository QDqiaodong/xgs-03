package com.plantcare.service;

import com.plantcare.dto.UserProfileDTO;
import com.plantcare.entity.Favorite;
import com.plantcare.repository.BrowseHistoryRepository;
import com.plantcare.repository.CareLogRepository;
import com.plantcare.repository.FavoriteRepository;
import com.plantcare.repository.PlantArchiveRepository;
import com.plantcare.repository.PlantCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final PlantArchiveRepository plantArchiveRepository;
    private final CareLogRepository careLogRepository;
    private final FavoriteRepository favoriteRepository;
    private final BrowseHistoryRepository browseHistoryRepository;
    private final PlantCategoryRepository plantCategoryRepository;

    public UserProfileDTO analyzeUserProfile(Long userId) {
        UserProfileDTO profile = new UserProfileDTO();
        profile.setUserId(userId);

        Long totalPlants = plantArchiveRepository.countByUserId(userId);
        profile.setTotalPlants(totalPlants != null ? totalPlants.intValue() : 0);

        LocalDate oneYearAgo = LocalDate.now().minusYears(1);
        Long totalCareLogs = careLogRepository.countByUserIdAndDateRange(userId, oneYearAgo, LocalDate.now());
        profile.setTotalCareLogs(totalCareLogs != null ? totalCareLogs.intValue() : 0);

        profile.setConsecutiveCareDays(calculateConsecutiveCareDays(userId));

        profile.setExperienceLevel(calculateExperienceLevel(profile));
        profile.setExperienceLevelName(getExperienceLevelName(profile.getExperienceLevel()));

        profile.setOwnedPlantCategoryIds(plantArchiveRepository.findDistinctPlantCategoryIdsByUserId(userId));

        Map<String, Double> categoryScores = calculateCategoryPreferenceScores(userId);
        profile.setCategoryPreferenceScores(categoryScores);
        profile.setPreferredCategories(getTopCategories(categoryScores, 3));

        profile.setPreferredLightRequirements(getTopLightRequirements(userId));
        profile.setPreferredWaterRequirements(getTopWaterRequirements(userId));

        Double avgDifficulty = plantArchiveRepository.findAvgDifficultyLevelByUserId(userId);
        profile.setAvgDifficultyLevel(avgDifficulty != null ? avgDifficulty.intValue() : 1);

        profile.setFavoritePlantCategoryIds(getFavoritePlantCategoryIds(userId));
        profile.setBrowsedPlantCategoryIds(getBrowsedPlantCategoryIds(userId));

        return profile;
    }

    private Integer calculateConsecutiveCareDays(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(365);
        List<LocalDate> dates = careLogRepository.findDistinctLogDatesByUserIdAndDateRange(userId, startDate, today);
        if (dates == null || dates.isEmpty()) {
            return 0;
        }

        Set<LocalDate> dateSet = new HashSet<>(dates);
        int maxConsecutive = 0;
        int currentConsecutive = 0;
        LocalDate current = startDate;

        while (!current.isAfter(today)) {
            if (dateSet.contains(current)) {
                currentConsecutive++;
                if (currentConsecutive > maxConsecutive) {
                    maxConsecutive = currentConsecutive;
                }
            } else {
                currentConsecutive = 0;
            }
            current = current.plusDays(1);
        }

        return maxConsecutive;
    }

    private Integer calculateExperienceLevel(UserProfileDTO profile) {
        int score = 0;

        if (profile.getTotalPlants() >= 1) score += 1;
        if (profile.getTotalPlants() >= 3) score += 1;
        if (profile.getTotalPlants() >= 5) score += 1;
        if (profile.getTotalPlants() >= 10) score += 1;

        if (profile.getTotalCareLogs() >= 5) score += 1;
        if (profile.getTotalCareLogs() >= 20) score += 1;
        if (profile.getTotalCareLogs() >= 50) score += 1;
        if (profile.getTotalCareLogs() >= 100) score += 1;

        if (profile.getConsecutiveCareDays() >= 3) score += 1;
        if (profile.getConsecutiveCareDays() >= 7) score += 1;
        if (profile.getConsecutiveCareDays() >= 30) score += 1;

        if (score <= 2) return 1;
        if (score <= 5) return 2;
        if (score <= 8) return 3;
        if (score <= 11) return 4;
        return 5;
    }

    private String getExperienceLevelName(Integer level) {
        switch (level) {
            case 1: return "新手入门";
            case 2: return "初级爱好者";
            case 3: return "中级养护者";
            case 4: return "高级爱好者";
            case 5: return "植物专家";
            default: return "新手入门";
        }
    }

    private Map<String, Double> calculateCategoryPreferenceScores(Long userId) {
        Map<String, Double> scores = new HashMap<>();

        List<Object[]> ownedCategoryCounts = plantArchiveRepository.countByCategoryAndUserId(userId);
        double maxOwnedCount = 1;
        if (ownedCategoryCounts != null && !ownedCategoryCounts.isEmpty()) {
            maxOwnedCount = ((Number) ownedCategoryCounts.get(0)[1]).doubleValue();
        }
        if (ownedCategoryCounts != null) {
            for (Object[] row : ownedCategoryCounts) {
                String category = (String) row[0];
                if (category == null) category = "未分类";
                double count = ((Number) row[1]).doubleValue();
                scores.merge(category, count / maxOwnedCount * 0.5, Double::sum);
            }
        }

        List<Object[]> careCategoryCounts = careLogRepository.countByCategoryAndUserIdAndDateRange(
                userId, LocalDate.now().minusYears(1), LocalDate.now());
        double maxCareCount = 1;
        if (careCategoryCounts != null && !careCategoryCounts.isEmpty()) {
            maxCareCount = ((Number) careCategoryCounts.get(0)[2]).doubleValue();
        }
        if (careCategoryCounts != null) {
            for (Object[] row : careCategoryCounts) {
                String category = (String) row[1];
                if (category == null) category = "未分类";
                double count = ((Number) row[2]).doubleValue();
                scores.merge(category, count / maxCareCount * 0.3, Double::sum);
            }
        }

        List<Long> favoriteIds = getFavoritePlantCategoryIds(userId);
        if (!favoriteIds.isEmpty()) {
            double favoriteWeight = 0.2 / favoriteIds.size();
            for (Long favId : favoriteIds) {
                plantCategoryRepository.findById(favId).ifPresent(pc -> {
                    if (pc.getCategory() != null) {
                        scores.merge(pc.getCategory(), favoriteWeight, Double::sum);
                    }
                });
            }
        }

        return scores;
    }

    private List<String> getTopCategories(Map<String, Double> categoryScores, int topN) {
        return categoryScores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(topN)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private List<String> getTopLightRequirements(Long userId) {
        List<Object[]> results = plantArchiveRepository.countByLightRequirementAndUserId(userId);
        if (results == null || results.isEmpty()) {
            return Collections.emptyList();
        }
        return results.stream()
                .sorted((a, b) -> Long.compare(
                        ((Number) b[1]).longValue(),
                        ((Number) a[1]).longValue()))
                .limit(3)
                .map(row -> (String) row[0])
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private List<String> getTopWaterRequirements(Long userId) {
        List<Object[]> results = plantArchiveRepository.countByWaterRequirementAndUserId(userId);
        if (results == null || results.isEmpty()) {
            return Collections.emptyList();
        }
        return results.stream()
                .sorted((a, b) -> Long.compare(
                        ((Number) b[1]).longValue(),
                        ((Number) a[1]).longValue()))
                .limit(3)
                .map(row -> (String) row[0])
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private List<Long> getFavoritePlantCategoryIds(Long userId) {
        List<Favorite> favorites = favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return favorites.stream()
                .filter(f -> "plant_category".equals(f.getTargetType()))
                .map(Favorite::getTargetId)
                .collect(Collectors.toList());
    }

    private List<Long> getBrowsedPlantCategoryIds(Long userId) {
        return browseHistoryRepository.findTopTargetIdsByUserIdAndTargetType(userId, "plant_category");
    }
}
