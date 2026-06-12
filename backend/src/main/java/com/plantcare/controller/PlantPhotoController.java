package com.plantcare.controller;

import com.plantcare.entity.PlantPhoto;
import com.plantcare.service.PlantPhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/plant-photos", produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
public class PlantPhotoController {

    private final PlantPhotoService plantPhotoService;

    @GetMapping("/archive/{plantArchiveId}")
    public ResponseEntity<List<PlantPhoto>> getPhotosByArchiveId(@PathVariable Long plantArchiveId) {
        return ResponseEntity.ok(plantPhotoService.getPhotosByArchiveId(plantArchiveId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlantPhoto> getPhotoById(@PathVariable Long id) {
        return plantPhotoService.getPhotoById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/archive/{plantArchiveId}/cover")
    public ResponseEntity<PlantPhoto> getCoverPhoto(@PathVariable Long plantArchiveId) {
        return plantPhotoService.getCoverPhoto(plantArchiveId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/upload")
    public ResponseEntity<PlantPhoto> uploadPhoto(
            @RequestParam("plantArchiveId") Long plantArchiveId,
            @RequestParam("userId") Long userId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "photoDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate photoDate) {
        return ResponseEntity.ok(plantPhotoService.uploadPhoto(plantArchiveId, userId, file, description, photoDate));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlantPhoto> updatePhoto(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates) {
        String description = (String) updates.get("description");
        LocalDate photoDate = updates.get("photoDate") != null
                ? LocalDate.parse((String) updates.get("photoDate"))
                : null;
        return ResponseEntity.ok(plantPhotoService.updatePhoto(id, description, photoDate));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePhoto(@PathVariable Long id) {
        plantPhotoService.deletePhoto(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/set-cover")
    public ResponseEntity<PlantPhoto> setCover(@PathVariable Long id) {
        return ResponseEntity.ok(plantPhotoService.setCover(id));
    }
}
