package com.plantcare.service;

import com.plantcare.entity.Favorite;
import com.plantcare.entity.FavoriteFolder;
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
    private final FavoriteFolderService favoriteFolderService;

    public List<Favorite> getUserFavorites(Long userId) {
        return favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Favorite> getFavoritesByFolder(Long userId, Long folderId) {
        if (folderId == null) {
            return favoriteRepository.findByUserIdAndFolderIdIsNullOrderByCreatedAtDesc(userId);
        }
        return favoriteRepository.findByUserIdAndFolderIdOrderByCreatedAtDesc(userId, folderId);
    }

    public Optional<Favorite> getFavorite(Long userId, String targetType, Long targetId) {
        return favoriteRepository.findByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId);
    }

    public Favorite addFavorite(Favorite favorite) {
        if (favorite.getFolderId() == null) {
            FavoriteFolder defaultFolder = favoriteFolderService.getOrCreateDefaultFolder(favorite.getUserId());
            favorite.setFolderId(defaultFolder.getId());
        }
        return favoriteRepository.save(favorite);
    }

    @Transactional
    public void removeFavorite(Long userId, String targetType, Long targetId) {
        favoriteRepository.deleteByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId);
    }

    @Transactional
    public void removeFavoriteFromFolder(Long userId, Long folderId, String targetType, Long targetId) {
        Optional<Favorite> favoriteOpt = favoriteRepository.findByUserIdAndTargetTypeAndTargetIdAndFolderId(userId, targetType, targetId, folderId);
        favoriteOpt.ifPresent(favoriteRepository::delete);
    }

    public boolean isFavorited(Long userId, String targetType, Long targetId) {
        return favoriteRepository.findByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId).isPresent();
    }

    public boolean isFavoritedInFolder(Long userId, Long folderId, String targetType, Long targetId) {
        return favoriteRepository.findByUserIdAndTargetTypeAndTargetIdAndFolderId(userId, targetType, targetId, folderId).isPresent();
    }

    @Transactional
    public Favorite moveFavorite(Long userId, Long favoriteId, Long targetFolderId) {
        Favorite favorite = favoriteRepository.findById(favoriteId)
                .orElseThrow(() -> new RuntimeException("收藏不存在"));
        
        if (!favorite.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此收藏");
        }
        
        if (targetFolderId != null) {
            favoriteFolderService.getFolderById(userId, targetFolderId)
                    .orElseThrow(() -> new RuntimeException("目标收藏夹不存在"));
        }
        
        favorite.setFolderId(targetFolderId);
        return favoriteRepository.save(favorite);
    }

    @Transactional
    public void moveFavoritesBatch(Long userId, Long sourceFolderId, Long targetFolderId) {
        if (targetFolderId != null) {
            favoriteFolderService.getFolderById(userId, targetFolderId)
                    .orElseThrow(() -> new RuntimeException("目标收藏夹不存在"));
        }
        
        List<Favorite> favorites = sourceFolderId == null 
                ? favoriteRepository.findByUserIdAndFolderIdIsNullOrderByCreatedAtDesc(userId)
                : favoriteRepository.findByUserIdAndFolderIdOrderByCreatedAtDesc(userId, sourceFolderId);
        
        for (Favorite favorite : favorites) {
            favorite.setFolderId(targetFolderId);
            favoriteRepository.save(favorite);
        }
    }
}
