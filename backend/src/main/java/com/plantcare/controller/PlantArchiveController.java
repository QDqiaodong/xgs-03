package com.plantcare.controller;

import com.plantcare.entity.PlantArchive;
import com.plantcare.service.PlantArchiveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/plant-archives", produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
public class PlantArchiveController {

    private final PlantArchiveService plantArchiveService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PlantArchive>> getUserArchives(@PathVariable Long userId) {
        return ResponseEntity.ok(plantArchiveService.getUserArchives(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlantArchive> getArchiveById(@PathVariable Long id) {
        return plantArchiveService.getArchiveById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PlantArchive> createArchive(@RequestBody PlantArchive archive) {
        return ResponseEntity.ok(plantArchiveService.createArchive(archive));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlantArchive> updateArchive(@PathVariable Long id, @RequestBody PlantArchive archive) {
        return ResponseEntity.ok(plantArchiveService.updateArchive(id, archive));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArchive(@PathVariable Long id) {
        plantArchiveService.deleteArchive(id);
        return ResponseEntity.ok().build();
    }
}
