package com.plantcare.controller;

import com.plantcare.dto.PlantRecommendationDTO;
import com.plantcare.dto.UserProfileDTO;
import com.plantcare.service.RecommendationService;
import com.plantcare.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/recommendations", produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final UserProfileService userProfileService;

    @GetMapping("/plants/{userId}")
    public ResponseEntity<List<PlantRecommendationDTO>> getPlantRecommendations(
            @PathVariable Long userId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        return ResponseEntity.ok(recommendationService.getRecommendations(userId, type, limit));
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<UserProfileDTO> getUserProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(userProfileService.analyzeUserProfile(userId));
    }

    @GetMapping("/plants/{userId}/similar")
    public ResponseEntity<List<PlantRecommendationDTO>> getSimilarRecommendations(
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        UserProfileDTO profile = userProfileService.analyzeUserProfile(userId);
        return ResponseEntity.ok(recommendationService.getSimilarRecommendations(profile, limit));
    }

    @GetMapping("/plants/{userId}/easy")
    public ResponseEntity<List<PlantRecommendationDTO>> getEasyCareRecommendations(
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        UserProfileDTO profile = userProfileService.analyzeUserProfile(userId);
        return ResponseEntity.ok(recommendationService.getEasyCareRecommendations(profile, limit));
    }

    @GetMapping("/plants/{userId}/popular")
    public ResponseEntity<List<PlantRecommendationDTO>> getPopularRecommendations(
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        UserProfileDTO profile = userProfileService.analyzeUserProfile(userId);
        return ResponseEntity.ok(recommendationService.getPopularRecommendations(profile, limit));
    }
}
