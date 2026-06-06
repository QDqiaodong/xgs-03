package com.plantcare.service;

import com.plantcare.entity.CareLog;
import com.plantcare.repository.CareLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CareLogService {

    private final CareLogRepository careLogRepository;

    public List<CareLog> getPlantLogs(Long plantArchiveId) {
        return careLogRepository.findByPlantArchiveIdOrderByLogDateDesc(plantArchiveId);
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
