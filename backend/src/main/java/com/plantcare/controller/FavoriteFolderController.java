package com.plantcare.controller;

import com.plantcare.entity.Favorite;
import com.plantcare.entity.FavoriteFolder;
import com.plantcare.service.FavoriteFolderService;
import com.plantcare.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/favorite-folders", produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
public class FavoriteFolderController {

    private final FavoriteFolderService favoriteFolderService;
    private final FavoriteService favoriteService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FavoriteFolder>> getUserFolders(@PathVariable Long userId) {
        return ResponseEntity.ok(favoriteFolderService.getUserFolders(userId));
    }

    @GetMapping("/{folderId}")
    public ResponseEntity<FavoriteFolder> getFolderById(
            @RequestParam Long userId,
            @PathVariable Long folderId) {
        return favoriteFolderService.getFolderById(userId, folderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<FavoriteFolder> createFolder(@RequestBody FavoriteFolder folder) {
        return ResponseEntity.ok(favoriteFolderService.createFolder(folder));
    }

    @PutMapping("/{folderId}")
    public ResponseEntity<FavoriteFolder> updateFolder(
            @RequestParam Long userId,
            @PathVariable Long folderId,
            @RequestBody FavoriteFolder folder) {
        return ResponseEntity.ok(favoriteFolderService.updateFolder(userId, folderId, folder));
    }

    @DeleteMapping("/{folderId}")
    public ResponseEntity<Void> deleteFolder(
            @RequestParam Long userId,
            @PathVariable Long folderId) {
        favoriteFolderService.deleteFolder(userId, folderId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/order")
    public ResponseEntity<Void> updateFolderOrder(
            @RequestParam Long userId,
            @RequestBody List<Long> folderIds) {
        favoriteFolderService.updateFolderOrder(userId, folderIds);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{folderId}/favorites")
    public ResponseEntity<List<Favorite>> getFavoritesByFolder(
            @RequestParam Long userId,
            @PathVariable Long folderId) {
        return ResponseEntity.ok(favoriteService.getFavoritesByFolder(userId, folderId));
    }

    @GetMapping("/{folderId}/count")
    public ResponseEntity<Long> getFolderFavoriteCount(
            @RequestParam Long userId,
            @PathVariable Long folderId) {
        return ResponseEntity.ok(favoriteFolderService.getFolderFavoriteCount(userId, folderId));
    }

    @PostMapping("/default")
    public ResponseEntity<FavoriteFolder> getOrCreateDefaultFolder(@RequestBody Map<String, Long> request) {
        Long userId = request.get("userId");
        return ResponseEntity.ok(favoriteFolderService.getOrCreateDefaultFolder(userId));
    }
}
