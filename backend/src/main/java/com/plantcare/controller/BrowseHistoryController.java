package com.plantcare.controller;

import com.plantcare.entity.BrowseHistory;
import com.plantcare.service.BrowseHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/browse-history", produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
public class BrowseHistoryController {

    private final BrowseHistoryService browseHistoryService;

    @PostMapping
    public ResponseEntity<BrowseHistory> recordBrowse(@RequestBody BrowseHistory browseHistory) {
        return ResponseEntity.ok(browseHistoryService.recordBrowse(
                browseHistory.getUserId(),
                browseHistory.getTargetType(),
                browseHistory.getTargetId()
        ));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BrowseHistory>> getUserBrowseHistory(@PathVariable Long userId) {
        return ResponseEntity.ok(browseHistoryService.getUserBrowseHistory(userId));
    }

    @GetMapping("/user/{userId}/type/{targetType}")
    public ResponseEntity<List<BrowseHistory>> getUserBrowseHistoryByType(
            @PathVariable Long userId,
            @PathVariable String targetType) {
        return ResponseEntity.ok(browseHistoryService.getUserBrowseHistoryByType(userId, targetType));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteBrowseHistory(
            @RequestParam Long userId,
            @RequestParam String targetType,
            @RequestParam Long targetId) {
        browseHistoryService.deleteBrowseHistory(userId, targetType, targetId);
        return ResponseEntity.ok().build();
    }
}
