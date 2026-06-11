package com.plantcare.service;

import com.plantcare.dto.PlantRecommendationDTO;
import com.plantcare.dto.RecommendationReasonDTO;
import com.plantcare.dto.UserProfileDTO;
import com.plantcare.entity.PlantCategory;
import com.plantcare.repository.PlantArchiveRepository;
import com.plantcare.repository.PlantCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final UserProfileService userProfileService;
    private final PlantCategoryRepository plantCategoryRepository;
    private final PlantArchiveRepository plantArchiveRepository;

    public List<PlantRecommendationDTO> getRecommendations(Long userId, String type, Integer limit) {
        if (limit == null || limit <= 0) limit = 10;
        UserProfileDTO profile = userProfileService.analyzeUserProfile(userId);

        if (type == null || "all".equalsIgnoreCase(type)) {
            return getMixedRecommendations(profile, limit);
        } else if ("similar".equalsIgnoreCase(type)) {
            return getSimilarRecommendations(profile, limit);
        } else if ("easy".equalsIgnoreCase(type)) {
            return getEasyCareRecommendations(profile, limit);
        } else if ("popular".equalsIgnoreCase(type)) {
            return getPopularRecommendations(profile, limit);
        } else {
            return getMixedRecommendations(profile, limit);
        }
    }

    public List<PlantRecommendationDTO> getMixedRecommendations(UserProfileDTO profile, int limit) {
        Map<Long, PlantRecommendationDTO> recommendationMap = new LinkedHashMap<>();

        List<PlantRecommendationDTO> similarRecs = getSimilarRecommendations(profile, limit * 2);
        for (PlantRecommendationDTO rec : similarRecs) {
            rec.setTotalScore(rec.getTotalScore() * 0.45);
            recommendationMap.put(rec.getPlantCategory().getId(), rec);
        }

        List<PlantRecommendationDTO> easyRecs = getEasyCareRecommendations(profile, limit * 2);
        for (PlantRecommendationDTO rec : easyRecs) {
            Long id = rec.getPlantCategory().getId();
            if (recommendationMap.containsKey(id)) {
                PlantRecommendationDTO existing = recommendationMap.get(id);
                existing.setTotalScore(existing.getTotalScore() + rec.getTotalScore() * 0.30);
                existing.getReasons().addAll(rec.getReasons());
                existing.setRecommendationType("mixed");
            } else {
                rec.setTotalScore(rec.getTotalScore() * 0.30);
                recommendationMap.put(id, rec);
            }
        }

        List<PlantRecommendationDTO> popularRecs = getPopularRecommendations(profile, limit * 2);
        for (PlantRecommendationDTO rec : popularRecs) {
            Long id = rec.getPlantCategory().getId();
            if (recommendationMap.containsKey(id)) {
                PlantRecommendationDTO existing = recommendationMap.get(id);
                existing.setTotalScore(existing.getTotalScore() + rec.getTotalScore() * 0.25);
                existing.getReasons().addAll(rec.getReasons());
                existing.setRecommendationType("mixed");
            } else {
                rec.setTotalScore(rec.getTotalScore() * 0.25);
                recommendationMap.put(id, rec);
            }
        }

        return recommendationMap.values().stream()
                .sorted(Comparator.comparingDouble(PlantRecommendationDTO::getTotalScore).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<PlantRecommendationDTO> getSimilarRecommendations(UserProfileDTO profile, int limit) {
        List<Long> excludeIds = new ArrayList<>(profile.getOwnedPlantCategoryIds());
        List<PlantCategory> candidates;

        if (profile.getPreferredCategories() != null && !profile.getPreferredCategories().isEmpty()) {
            candidates = plantCategoryRepository.findByCategoriesExcludingIds(
                    profile.getPreferredCategories(), excludeIds, PageRequest.of(0, limit * 3));
        } else {
            List<Long> favoriteAndBrowsed = new ArrayList<>();
            if (profile.getFavoritePlantCategoryIds() != null) {
                favoriteAndBrowsed.addAll(profile.getFavoritePlantCategoryIds());
            }
            if (profile.getBrowsedPlantCategoryIds() != null) {
                favoriteAndBrowsed.addAll(profile.getBrowsedPlantCategoryIds());
            }
            if (!favoriteAndBrowsed.isEmpty()) {
                Set<String> categories = new HashSet<>();
                for (Long cid : favoriteAndBrowsed) {
                    plantCategoryRepository.findById(cid).ifPresent(pc -> {
                        if (pc.getCategory() != null) categories.add(pc.getCategory());
                    });
                }
                if (!categories.isEmpty()) {
                    candidates = plantCategoryRepository.findByCategoriesExcludingIds(
                            new ArrayList<>(categories), excludeIds, PageRequest.of(0, limit * 3));
                } else {
                    candidates = plantCategoryRepository.findPopularExcludingIds(excludeIds, PageRequest.of(0, limit * 3));
                }
            } else {
                candidates = plantCategoryRepository.findPopularExcludingIds(excludeIds, PageRequest.of(0, limit * 3));
            }
        }

        List<Long> collaborativeUserIds = plantArchiveRepository.findUserIdsWithSamePlantCategories(
                profile.getOwnedPlantCategoryIds(), profile.getUserId());

        Set<Long> collaborativeCategoryIds = new HashSet<>();
        if (!collaborativeUserIds.isEmpty() && collaborativeUserIds.size() <= 20) {
            for (Long similarUserId : collaborativeUserIds) {
                List<Long> similarUserOwned = plantArchiveRepository.findDistinctPlantCategoryIdsByUserId(similarUserId);
                collaborativeCategoryIds.addAll(similarUserOwned);
            }
            collaborativeCategoryIds.removeAll(excludeIds);
        }

        List<PlantRecommendationDTO> recommendations = new ArrayList<>();
        for (PlantCategory pc : candidates) {
            List<RecommendationReasonDTO> reasons = new ArrayList<>();
            double score = 0.0;

            if (profile.getPreferredCategories() != null && profile.getPreferredCategories().contains(pc.getCategory())) {
                Double prefScore = profile.getCategoryPreferenceScores().get(pc.getCategory());
                if (prefScore != null) {
                    score += prefScore * 0.5;
                    reasons.add(new RecommendationReasonDTO(
                            "category_match",
                            "与您喜欢的「" + pc.getCategory() + "」同类植物",
                            prefScore * 0.5
                    ));
                }
            }

            if (profile.getPreferredLightRequirements() != null &&
                    profile.getPreferredLightRequirements().contains(pc.getLightRequirement())) {
                score += 0.15;
                reasons.add(new RecommendationReasonDTO(
                        "light_match",
                        "光照需求「" + pc.getLightRequirement() + "」符合您的养护习惯",
                        0.15
                ));
            }

            if (profile.getPreferredWaterRequirements() != null &&
                    profile.getPreferredWaterRequirements().contains(pc.getWaterRequirement())) {
                score += 0.15;
                reasons.add(new RecommendationReasonDTO(
                        "water_match",
                        "浇水需求「" + pc.getWaterRequirement() + "」符合您的养护习惯",
                        0.15
                ));
            }

            if (collaborativeCategoryIds.contains(pc.getId())) {
                score += 0.20;
                reasons.add(new RecommendationReasonDTO(
                        "collaborative",
                        "与您品味相似的用户也在养这种植物",
                        0.20
                ));
            }

            if (score > 0) {
                PlantRecommendationDTO dto = new PlantRecommendationDTO();
                dto.setPlantCategory(pc);
                dto.setTotalScore(score);
                dto.setRecommendationType("similar");
                dto.setReasons(reasons);
                recommendations.add(dto);
            }
        }

        recommendations.sort(Comparator.comparingDouble(PlantRecommendationDTO::getTotalScore).reversed());
        return recommendations.stream().limit(limit).collect(Collectors.toList());
    }

    public List<PlantRecommendationDTO> getEasyCareRecommendations(UserProfileDTO profile, int limit) {
        int maxDifficulty = Math.max(profile.getExperienceLevel() + 1, 2);
        List<Long> excludeIds = new ArrayList<>(profile.getOwnedPlantCategoryIds());

        List<PlantCategory> easyPlants = plantCategoryRepository.findEasyCareExcludingIds(
                maxDifficulty, excludeIds, PageRequest.of(0, limit * 3));

        List<PlantRecommendationDTO> recommendations = new ArrayList<>();
        for (PlantCategory pc : easyPlants) {
            List<RecommendationReasonDTO> reasons = new ArrayList<>();
            double score = 0.0;

            int difficultyLevel = pc.getDifficultyLevel() != null ? pc.getDifficultyLevel() : 1;
            if (difficultyLevel <= 1) {
                score += 0.5;
                reasons.add(new RecommendationReasonDTO(
                        "very_easy",
                        "入门级植物，养护非常简单",
                        0.5
                ));
            } else if (difficultyLevel <= 2) {
                score += 0.4;
                reasons.add(new RecommendationReasonDTO(
                        "easy",
                        "养护简单，适合新手",
                        0.4
                ));
            } else {
                double easeScore = 0.3 * (5.0 - difficultyLevel) / 3.0;
                score += easeScore;
                reasons.add(new RecommendationReasonDTO(
                        "manageable",
                        "难度适中，在您的经验范围内",
                        easeScore
                ));
            }

            if (profile.getExperienceLevel() <= 2 && difficultyLevel <= 1) {
                score += 0.25;
                reasons.add(new RecommendationReasonDTO(
                        "beginner_friendly",
                        "特别推荐给新手养护者",
                        0.25
                ));
            }

            double popularityBonus = (pc.getPopularityScore() != null ? pc.getPopularityScore() : 0) / 200.0;
            popularityBonus = Math.min(popularityBonus, 0.25);
            score += popularityBonus;
            if (popularityBonus > 0.1) {
                reasons.add(new RecommendationReasonDTO(
                        "popular_easy",
                        "广受欢迎的易养植物",
                        popularityBonus
                ));
            }

            PlantRecommendationDTO dto = new PlantRecommendationDTO();
            dto.setPlantCategory(pc);
            dto.setTotalScore(score);
            dto.setRecommendationType("easy");
            dto.setReasons(reasons);
            recommendations.add(dto);
        }

        recommendations.sort(Comparator.comparingDouble(PlantRecommendationDTO::getTotalScore).reversed());
        return recommendations.stream().limit(limit).collect(Collectors.toList());
    }

    public List<PlantRecommendationDTO> getPopularRecommendations(UserProfileDTO profile, int limit) {
        List<Long> excludeIds = new ArrayList<>(profile.getOwnedPlantCategoryIds());

        List<PlantCategory> popularPlants = plantCategoryRepository.findPopularExcludingIds(
                excludeIds, PageRequest.of(0, limit * 3));

        List<PlantRecommendationDTO> recommendations = new ArrayList<>();
        for (PlantCategory pc : popularPlants) {
            List<RecommendationReasonDTO> reasons = new ArrayList<>();
            double score = 0.0;

            int popularity = pc.getPopularityScore() != null ? pc.getPopularityScore() : 0;
            double popularityScore = popularity / 100.0;
            popularityScore = Math.min(popularityScore, 0.6);
            score += popularityScore;

            if (popularity >= 90) {
                reasons.add(new RecommendationReasonDTO(
                        "trending",
                        "超级热门植物，全网关注度极高",
                        popularityScore
                ));
            } else if (popularity >= 75) {
                reasons.add(new RecommendationReasonDTO(
                        "very_popular",
                        "非常受欢迎的热门品种",
                        popularityScore
                ));
            } else {
                reasons.add(new RecommendationReasonDTO(
                        "popular",
                        "人气较高的植物品种",
                        popularityScore
                ));
            }

            if (profile.getPreferredCategories() != null && profile.getPreferredCategories().contains(pc.getCategory())) {
                score += 0.25;
                reasons.add(new RecommendationReasonDTO(
                        "popular_in_category",
                        "您喜爱的「" + pc.getCategory() + "」分类中的热门款",
                        0.25
                ));
            }

            int difficulty = pc.getDifficultyLevel() != null ? pc.getDifficultyLevel() : 3;
            if (difficulty <= profile.getExperienceLevel() + 1) {
                score += 0.15;
                reasons.add(new RecommendationReasonDTO(
                        "match_skill",
                        "养护难度匹配您的经验水平",
                        0.15
                ));
            }

            PlantRecommendationDTO dto = new PlantRecommendationDTO();
            dto.setPlantCategory(pc);
            dto.setTotalScore(score);
            dto.setRecommendationType("popular");
            dto.setReasons(reasons);
            recommendations.add(dto);
        }

        recommendations.sort(Comparator.comparingDouble(PlantRecommendationDTO::getTotalScore).reversed());
        return recommendations.stream().limit(limit).collect(Collectors.toList());
    }
}
