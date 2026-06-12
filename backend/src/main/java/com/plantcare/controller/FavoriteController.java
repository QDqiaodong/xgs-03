package com.plantcare.controller;

import com.plantcare.entity.Favorite;
import com.plantcare.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/favorites", produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Favorite>> getUserFavorites(@PathVariable Long userId) {
        return ResponseEntity.ok(favoriteService.getUserFavorites(userId));
    }

    @GetMapping("/folder/{folderId}")
    public ResponseEntity<List<Favorite>> getFavoritesByFolder(
            @RequestParam Long userId,
            @PathVariable Long folderId) {
        return ResponseEntity.ok(favoriteService.getFavoritesByFolder(userId, folderId));
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> isFavorited(
            @RequestParam Long userId,
            @RequestParam String targetType,
            @RequestParam Long targetId) {
        return ResponseEntity.ok(favoriteService.isFavorited(userId, targetType, targetId));
    }

    @GetMapping("/check-folder")
    public ResponseEntity<Boolean> isFavoritedInFolder(
            @RequestParam Long userId,
            @RequestParam Long folderId,
            @RequestParam String targetType,
            @RequestParam Long targetId) {
        return ResponseEntity.ok(favoriteService.isFavoritedInFolder(userId, folderId, targetType, targetId));
    }

    @PostMapping
    public ResponseEntity<Favorite> addFavorite(@RequestBody Favorite favorite) {
        return ResponseEntity.ok(favoriteService.addFavorite(favorite));
    }

    @DeleteMapping
    public ResponseEntity<Void> removeFavorite(
            @RequestParam Long userId,
            @RequestParam String targetType,
            @RequestParam Long targetId) {
        favoriteService.removeFavorite(userId, targetType, targetId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/folder/{folderId}")
    public ResponseEntity<Void> removeFavoriteFromFolder(
            @RequestParam Long userId,
            @PathVariable Long folderId,
            @RequestParam String targetType,
            @RequestParam Long targetId) {
        favoriteService.removeFavoriteFromFolder(userId, folderId, targetType, targetId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{favoriteId}/move")
    public ResponseEntity<Favorite> moveFavorite(
            @RequestParam Long userId,
            @PathVariable Long favoriteId,
            @RequestBody Map<String, Long> request) {
        Long targetFolderId = request.get("targetFolderId");
        return ResponseEntity.ok(favoriteService.moveFavorite(userId, favoriteId, targetFolderId));
    }

    @PutMapping("/move-batch")
    public ResponseEntity<Void> moveFavoritesBatch(
            @RequestParam Long userId,
            @RequestParam Long sourceFolderId,
            @RequestParam(required = false) Long targetFolderId) {
        favoriteService.moveFavoritesBatch(userId, sourceFolderId, targetFolderId);
        return ResponseEntity.ok().build();
    }
}
