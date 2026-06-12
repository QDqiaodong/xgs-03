package com.plantcare.controller;

import com.plantcare.entity.PlantArchive;
import com.plantcare.entity.Tag;
import com.plantcare.service.PlantArchiveTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/plant-archive-tags", produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
public class PlantArchiveTagController {

    private final PlantArchiveTagService plantArchiveTagService;

    @GetMapping("/plant/{plantArchiveId}")
    public ResponseEntity<List<Tag>> getTagsForPlantArchive(@PathVariable Long plantArchiveId) {
        return ResponseEntity.ok(plantArchiveTagService.getTagsForPlantArchive(plantArchiveId));
    }

    @GetMapping("/plants/batch")
    public ResponseEntity<Map<Long, List<Tag>>> getTagsForPlantArchives(@RequestParam List<Long> plantArchiveIds) {
        return ResponseEntity.ok(plantArchiveTagService.getTagsForPlantArchives(plantArchiveIds));
    }

    @PostMapping
    public ResponseEntity<?> addTagToPlantArchive(@RequestBody Map<String, Long> request) {
        try {
            Long plantArchiveId = request.get("plantArchiveId");
            Long tagId = request.get("tagId");
            if (plantArchiveId == null || tagId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "plantArchiveId和tagId不能为空"));
            }
            return ResponseEntity.ok(plantArchiveTagService.addTagToPlantArchive(plantArchiveId, tagId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/plant/{plantArchiveId}")
    public ResponseEntity<List<Tag>> setTagsForPlantArchive(
            @PathVariable Long plantArchiveId,
            @RequestBody(required = false) List<Long> tagIds) {
        return ResponseEntity.ok(plantArchiveTagService.setTagsForPlantArchive(plantArchiveId, tagIds));
    }

    @DeleteMapping
    public ResponseEntity<Void> removeTagFromPlantArchive(
            @RequestParam Long plantArchiveId,
            @RequestParam Long tagId) {
        plantArchiveTagService.removeTagFromPlantArchive(plantArchiveId, tagId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{userId}/tag/{tagId}")
    public ResponseEntity<List<PlantArchive>> getPlantArchivesByTag(
            @PathVariable Long userId,
            @PathVariable Long tagId) {
        return ResponseEntity.ok(plantArchiveTagService.getPlantArchivesByTag(userId, tagId));
    }

    @GetMapping("/user/{userId}/tags")
    public ResponseEntity<List<PlantArchive>> getPlantArchivesByTags(
            @PathVariable Long userId,
            @RequestParam(required = false) List<Long> tagIds) {
        return ResponseEntity.ok(plantArchiveTagService.getPlantArchivesByTags(userId, tagIds));
    }

    @GetMapping("/tag/{tagId}/count")
    public ResponseEntity<Long> getPlantCountForTag(@PathVariable Long tagId) {
        return ResponseEntity.ok(plantArchiveTagService.getPlantCountForTag(tagId));
    }

    @GetMapping("/tags/count")
    public ResponseEntity<Map<Long, Long>> getPlantCountsForTags(@RequestParam List<Long> tagIds) {
        return ResponseEntity.ok(plantArchiveTagService.getPlantCountsForTags(tagIds));
    }
}
