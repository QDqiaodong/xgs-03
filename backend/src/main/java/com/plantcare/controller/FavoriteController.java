package com.plantcare.controller;

import com.plantcare.entity.Favorite;
import com.plantcare.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/favorites", produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Favorite>> getUserFavorites(@PathVariable Long userId) {
        return ResponseEntity.ok(favoriteService.getUserFavorites(userId));
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> isFavorited(
            @RequestParam Long userId,
            @RequestParam String targetType,
            @RequestParam Long targetId) {
        return ResponseEntity.ok(favoriteService.isFavorited(userId, targetType, targetId));
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
}
