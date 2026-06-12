package com.plantcare.service;

import com.plantcare.entity.Favorite;
import com.plantcare.entity.FavoriteFolder;
import com.plantcare.repository.FavoriteFolderRepository;
import com.plantcare.repository.FavoriteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FavoriteFolderService {

    private final FavoriteFolderRepository favoriteFolderRepository;
    private final FavoriteRepository favoriteRepository;

    public List<FavoriteFolder> getUserFolders(Long userId) {
        return favoriteFolderRepository.findByUserIdOrderBySortOrderAscCreatedAtAsc(userId);
    }

    public Optional<FavoriteFolder> getFolderById(Long userId, Long folderId) {
        return favoriteFolderRepository.findByUserIdAndId(userId, folderId);
    }

    public FavoriteFolder createFolder(FavoriteFolder folder) {
        if (folder.getSortOrder() == null) {
            List<FavoriteFolder> existingFolders = favoriteFolderRepository.findByUserIdOrderBySortOrderAscCreatedAtAsc(folder.getUserId());
            folder.setSortOrder(existingFolders.size());
        }
        return favoriteFolderRepository.save(folder);
    }

    public FavoriteFolder updateFolder(Long userId, Long folderId, FavoriteFolder folder) {
        FavoriteFolder existing = favoriteFolderRepository.findByUserIdAndId(userId, folderId)
                .orElseThrow(() -> new RuntimeException("收藏夹不存在"));
        
        if (existing.getIsDefault() && !folder.getIsDefault()) {
            throw new RuntimeException("默认收藏夹不能取消默认状态");
        }
        
        existing.setName(folder.getName());
        existing.setDescription(folder.getDescription());
        if (folder.getSortOrder() != null) {
            existing.setSortOrder(folder.getSortOrder());
        }
        
        return favoriteFolderRepository.save(existing);
    }

    @Transactional
    public void deleteFolder(Long userId, Long folderId) {
        FavoriteFolder folder = favoriteFolderRepository.findByUserIdAndId(userId, folderId)
                .orElseThrow(() -> new RuntimeException("收藏夹不存在"));
        
        if (folder.getIsDefault()) {
            throw new RuntimeException("默认收藏夹不能删除");
        }
        
        favoriteRepository.deleteByUserIdAndFolderId(userId, folderId);
        favoriteFolderRepository.deleteByUserIdAndId(userId, folderId);
    }

    @Transactional
    public void updateFolderOrder(Long userId, List<Long> folderIds) {
        List<FavoriteFolder> folders = favoriteFolderRepository.findByUserIdOrderBySortOrderAscCreatedAtAsc(userId);
        
        for (int i = 0; i < folderIds.size(); i++) {
            Long folderId = folderIds.get(i);
            for (FavoriteFolder folder : folders) {
                if (folder.getId().equals(folderId)) {
                    folder.setSortOrder(i);
                    favoriteFolderRepository.save(folder);
                    break;
                }
            }
        }
    }

    public long getFolderFavoriteCount(Long userId, Long folderId) {
        return favoriteRepository.countByUserIdAndFolderId(userId, folderId);
    }

    public FavoriteFolder getOrCreateDefaultFolder(Long userId) {
        Optional<FavoriteFolder> defaultFolder = favoriteFolderRepository.findByUserIdAndIsDefaultTrue(userId);
        if (defaultFolder.isPresent()) {
            return defaultFolder.get();
        }
        
        FavoriteFolder folder = new FavoriteFolder();
        folder.setUserId(userId);
        folder.setName("默认收藏夹");
        folder.setDescription("系统默认收藏夹");
        folder.setIsDefault(true);
        folder.setSortOrder(0);
        
        return favoriteFolderRepository.save(folder);
    }
}
