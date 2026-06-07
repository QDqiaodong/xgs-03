package com.plantcare.service;

import com.plantcare.entity.CareLog;
import com.plantcare.repository.CareLogRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CareLogService {

    private final CareLogRepository careLogRepository;

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
        return careLogRepository.save(log);
    }

    public CareLog updateLog(Long id, CareLog log) {
        log.setId(id);
        return careLogRepository.save(log);
    }

    public void deleteLog(Long id) {
        careLogRepository.deleteById(id);
    }
}
