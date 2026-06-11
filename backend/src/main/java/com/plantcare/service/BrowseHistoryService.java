package com.plantcare.service;

import com.plantcare.entity.BrowseHistory;
import com.plantcare.repository.BrowseHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BrowseHistoryService {

    private final BrowseHistoryRepository browseHistoryRepository;

    @Transactional
    public BrowseHistory recordBrowse(Long userId, String targetType, Long targetId) {
        Optional<BrowseHistory> existing = browseHistoryRepository
                .findByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId);

        if (existing.isPresent()) {
            BrowseHistory history = existing.get();
            history.setViewCount(history.getViewCount() + 1);
            history.setLastViewedAt(LocalDateTime.now());
            return browseHistoryRepository.save(history);
        } else {
            BrowseHistory history = new BrowseHistory();
            history.setUserId(userId);
            history.setTargetType(targetType);
            history.setTargetId(targetId);
            history.setViewCount(1);
            history.setLastViewedAt(LocalDateTime.now());
            return browseHistoryRepository.save(history);
        }
    }

    public List<BrowseHistory> getUserBrowseHistory(Long userId) {
        return browseHistoryRepository.findByUserIdOrderByLastViewedAtDesc(userId);
    }

    public List<BrowseHistory> getUserBrowseHistoryByType(Long userId, String targetType) {
        return browseHistoryRepository.findByUserIdAndTargetTypeOrderByLastViewedAtDesc(userId, targetType);
    }

    public List<Long> getTopBrowsedPlantCategoryIds(Long userId) {
        return browseHistoryRepository.findTopTargetIdsByUserIdAndTargetType(userId, "plant_category");
    }

    public Optional<BrowseHistory> getBrowseHistory(Long userId, String targetType, Long targetId) {
        return browseHistoryRepository.findByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId);
    }

    @Transactional
    public void deleteBrowseHistory(Long userId, String targetType, Long targetId) {
        browseHistoryRepository.deleteByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId);
    }
}
