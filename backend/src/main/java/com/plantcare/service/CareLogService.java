package com.plantcare.service;

import com.plantcare.dto.CareStatisticsDTO;
import com.plantcare.dto.CategoryCareDistribution;
import com.plantcare.dto.MonthlyTrendItem;
import com.plantcare.dto.OperationTypeStats;
import com.plantcare.dto.PlantCareRanking;
import com.plantcare.entity.CareLog;
import com.plantcare.entity.PlantArchive;
import com.plantcare.repository.CareLogRepository;
import com.plantcare.repository.PlantArchiveRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CareLogService {

    private static final String CACHE_NAME = "careStatistics";

    private final CareLogRepository careLogRepository;
    private final PlantArchiveRepository plantArchiveRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public List<CareLog> getPlantLogs(Long plantArchiveId) {
        return careLogRepository.findByPlantArchiveIdOrderByLogDateDescCreatedAtDesc(plantArchiveId);
    }

    public Page<CareLog> getPlantLogsPaged(Long plantArchiveId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return careLogRepository.findByPlantArchiveIdOrderByLogDateDescCreatedAtDesc(plantArchiveId, pageable);
    }

    public Page<CareLog> getPlantLogsPagedFiltered(
            Long plantArchiveId,
            int page,
            int size,
            List<String> operationTypes,
            LocalDate startDate,
            LocalDate endDate,
            String keyword
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(
                Sort.Order.desc("logDate"),
                Sort.Order.desc("createdAt")
        ));

        Specification<CareLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("plantArchiveId"), plantArchiveId));

            if (operationTypes != null && !operationTypes.isEmpty()) {
                predicates.add(root.get("operationType").in(operationTypes));
            }

            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("logDate"), startDate));
            }

            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("logDate"), endDate));
            }

            if (keyword != null && !keyword.trim().isEmpty()) {
                String likePattern = "%" + keyword.trim().toLowerCase() + "%";
                Predicate detailsLike = cb.like(cb.lower(root.get("details")), likePattern);
                Predicate growthLike = cb.like(cb.lower(root.get("growthStatus")), likePattern);
                Predicate diseaseLike = cb.like(cb.lower(root.get("diseaseStatus")), likePattern);
                predicates.add(cb.or(detailsLike, growthLike, diseaseLike));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return careLogRepository.findAll(spec, pageable);
    }

    public List<CareLog> getUserLogs(Long userId) {
        return careLogRepository.findByUserIdOrderByLogDateDesc(userId);
    }

    public Optional<CareLog> getLogById(Long id) {
        return careLogRepository.findById(id);
    }

    public CareLog createLog(CareLog log) {
        CareLog saved = careLogRepository.save(log);
        evictUserStatisticsCache(log.getUserId());
        return saved;
    }

    public CareLog updateLog(Long id, CareLog log) {
        log.setId(id);
        CareLog saved = careLogRepository.save(log);
        evictUserStatisticsCache(saved.getUserId());
        return saved;
    }

    public void deleteLog(Long id) {
        Optional<CareLog> logOpt = careLogRepository.findById(id);
        careLogRepository.deleteById(id);
        if (logOpt.isPresent()) {
            evictUserStatisticsCache(logOpt.get().getUserId());
        }
    }

    private void evictUserStatisticsCache(Long userId) {
        String pattern = CACHE_NAME + "::" + userId + "_*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    @Cacheable(value = CACHE_NAME, key = "#userId + '_' + (#startDate != null ? #startDate.toString() : 'null') + '_' + (#endDate != null ? #endDate.toString() : 'null')")
    public CareStatisticsDTO getStatistics(Long userId, LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            startDate = LocalDate.now().minusYears(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        CareStatisticsDTO stats = new CareStatisticsDTO();
        stats.setUserId(userId);
        stats.setStartDate(startDate);
        stats.setEndDate(endDate);

        Long totalCount = careLogRepository.countByUserIdAndDateRange(userId, startDate, endDate);
        stats.setTotalCareCount(totalCount != null ? totalCount : 0L);

        stats.setConsecutiveCareDays(calculateConsecutiveDays(userId, startDate, endDate));

        stats.setOperationTypeStats(calculateOperationTypeStats(userId, startDate, endDate, stats.getTotalCareCount()));

        stats.setMostFrequentPlant(findMostFrequentPlant(userId, startDate, endDate));

        stats.setMonthlyTrend(calculateMonthlyTrend(userId, startDate, endDate));

        stats.setCategoryDistribution(calculateCategoryDistribution(userId, startDate, endDate, stats.getTotalCareCount()));

        return stats;
    }

    private Integer calculateConsecutiveDays(Long userId, LocalDate startDate, LocalDate endDate) {
        List<LocalDate> dates = careLogRepository.findDistinctLogDatesByUserIdAndDateRange(userId, startDate, endDate);
        if (dates == null || dates.isEmpty()) {
            return 0;
        }

        Set<LocalDate> dateSet = new HashSet<>(dates);
        int maxConsecutive = 0;
        int currentConsecutive = 0;
        LocalDate current = startDate;

        while (!current.isAfter(endDate)) {
            if (dateSet.contains(current)) {
                currentConsecutive++;
                if (currentConsecutive > maxConsecutive) {
                    maxConsecutive = currentConsecutive;
                }
            } else {
                currentConsecutive = 0;
            }
            current = current.plusDays(1);
        }

        return maxConsecutive;
    }

    private List<OperationTypeStats> calculateOperationTypeStats(Long userId, LocalDate startDate, LocalDate endDate, Long totalCount) {
        List<Object[]> results = careLogRepository.countByOperationTypeAndUserIdAndDateRange(userId, startDate, endDate);
        List<OperationTypeStats> stats = new ArrayList<>();

        if (results != null) {
            for (Object[] row : results) {
                String opType = (String) row[0];
                Long count = ((Number) row[1]).longValue();
                double percentage = totalCount > 0 ? (count * 100.0 / totalCount) : 0.0;
                stats.add(new OperationTypeStats(opType, count, Math.round(percentage * 100.0) / 100.0));
            }
        }

        stats.sort(Comparator.comparingLong(OperationTypeStats::getCount).reversed());
        return stats;
    }

    private PlantCareRanking findMostFrequentPlant(Long userId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> results = careLogRepository.countByPlantArchiveIdAndUserIdAndDateRange(userId, startDate, endDate);
        if (results == null || results.isEmpty()) {
            return null;
        }

        Object[] top = results.get(0);
        Long plantId = ((Number) top[0]).longValue();
        Long count = ((Number) top[1]).longValue();

        Optional<PlantArchive> archiveOpt = plantArchiveRepository.findById(plantId);
        if (archiveOpt.isPresent()) {
            PlantArchive archive = archiveOpt.get();
            return new PlantCareRanking(plantId, archive.getCustomName(), archive.getImageUrl(), count);
        }

        return new PlantCareRanking(plantId, "未知植物", null, count);
    }

    private List<MonthlyTrendItem> calculateMonthlyTrend(Long userId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> results = careLogRepository.countByMonthAndUserIdAndDateRange(userId, startDate, endDate);
        List<MonthlyTrendItem> trend = new ArrayList<>();

        if (results != null) {
            for (Object[] row : results) {
                String month = (String) row[0];
                Long count = ((Number) row[1]).longValue();
                trend.add(new MonthlyTrendItem(month, count));
            }
        }

        return trend;
    }

    private List<CategoryCareDistribution> calculateCategoryDistribution(Long userId, LocalDate startDate, LocalDate endDate, Long totalCount) {
        List<Object[]> results = careLogRepository.countByCategoryAndUserIdAndDateRange(userId, startDate, endDate);
        List<CategoryCareDistribution> distribution = new ArrayList<>();

        if (results != null) {
            for (Object[] row : results) {
                Long categoryId = row[0] != null ? ((Number) row[0]).longValue() : null;
                String categoryName = row[1] != null ? (String) row[1] : "未分类";
                Long count = ((Number) row[2]).longValue();
                double percentage = totalCount > 0 ? (count * 100.0 / totalCount) : 0.0;
                distribution.add(new CategoryCareDistribution(categoryId, categoryName, count, Math.round(percentage * 100.0) / 100.0));
            }
        }

        return distribution;
    }
}
