package com.plantcare.service;

import com.plantcare.entity.Favorite;
import com.plantcare.repository.FavoriteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;

    public List<Favorite> getUserFavorites(Long userId) {
        return favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Optional<Favorite> getFavorite(Long userId, String targetType, Long targetId) {
        return favoriteRepository.findByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId);
    }

    public Favorite addFavorite(Favorite favorite) {
        return favoriteRepository.save(favorite);
    }

    @Transactional
    public void removeFavorite(Long userId, String targetType, Long targetId) {
        favoriteRepository.deleteByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId);
    }

    public boolean isFavorited(Long userId, String targetType, Long targetId) {
        return favoriteRepository.findByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId).isPresent();
    }
}
