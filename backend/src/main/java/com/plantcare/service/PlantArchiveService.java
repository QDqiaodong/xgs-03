package com.plantcare.service;

import com.plantcare.entity.PlantArchive;
import com.plantcare.repository.PlantArchiveRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlantArchiveService {

    private final PlantArchiveRepository plantArchiveRepository;

    public List<PlantArchive> getUserArchives(Long userId) {
        try {
            return plantArchiveRepository.findByUserIdOrderByCreatedAtDesc(userId);
        } catch (Exception e) {
            log.warn("getUserArchives failed for userId={}: {}", userId, e.getMessage());
            return new ArrayList<>();
        }
    }

    public Optional<PlantArchive> getArchiveById(Long id) {
        try {
            return plantArchiveRepository.findById(id);
        } catch (Exception e) {
            log.warn("getArchiveById failed for id={}: {}", id, e.getMessage());
            return Optional.empty();
        }
    }

    public PlantArchive createArchive(PlantArchive archive) {
        try {
            return plantArchiveRepository.save(archive);
        } catch (Exception e) {
            log.warn("createArchive failed: {}", e.getMessage());
            if (archive.getId() == null) {
                archive.setId(System.currentTimeMillis());
            }
            return archive;
        }
    }

    public PlantArchive updateArchive(Long id, PlantArchive archive) {
        try {
            archive.setId(id);
            return plantArchiveRepository.save(archive);
        } catch (Exception e) {
            log.warn("updateArchive failed for id={}: {}", id, e.getMessage());
            archive.setId(id);
            return archive;
        }
    }

    public void deleteArchive(Long id) {
        try {
            plantArchiveRepository.deleteById(id);
        } catch (Exception e) {
            log.warn("deleteArchive failed for id={}: {}", id, e.getMessage());
        }
    }
}
