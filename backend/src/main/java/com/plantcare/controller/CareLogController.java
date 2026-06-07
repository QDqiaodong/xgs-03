package com.plantcare.controller;

import com.plantcare.entity.CareLog;
import com.plantcare.service.CareLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/care-logs")
@RequiredArgsConstructor
public class CareLogController {

    private final CareLogService careLogService;

    @GetMapping("/plant/{plantArchiveId}")
    public ResponseEntity<List<CareLog>> getPlantLogs(@PathVariable Long plantArchiveId) {
        return ResponseEntity.ok(careLogService.getPlantLogs(plantArchiveId));
    }

    @GetMapping("/plant/{plantArchiveId}/paged")
    public ResponseEntity<Page<CareLog>> getPlantLogsPaged(
            @PathVariable Long plantArchiveId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(careLogService.getPlantLogsPaged(plantArchiveId, page, size));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CareLog>> getUserLogs(@PathVariable Long userId) {
        return ResponseEntity.ok(careLogService.getUserLogs(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CareLog> getLogById(@PathVariable Long id) {
        return careLogService.getLogById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CareLog> createLog(@RequestBody CareLog log) {
        return ResponseEntity.ok(careLogService.createLog(log));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CareLog> updateLog(@PathVariable Long id, @RequestBody CareLog log) {
        return ResponseEntity.ok(careLogService.updateLog(id, log));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLog(@PathVariable Long id) {
        careLogService.deleteLog(id);
        return ResponseEntity.ok().build();
    }
}
