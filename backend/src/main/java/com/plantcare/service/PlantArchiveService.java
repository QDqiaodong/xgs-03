package com.plantcare.service;

import com.plantcare.entity.PlantArchive;
import com.plantcare.repository.PlantArchiveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PlantArchiveService {

    private final PlantArchiveRepository plantArchiveRepository;

    public List<PlantArchive> getUserArchives(Long userId) {
        return plantArchiveRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Optional<PlantArchive> getArchiveById(Long id) {
        return plantArchiveRepository.findById(id);
    }

    public PlantArchive createArchive(PlantArchive archive) {
        return plantArchiveRepository.save(archive);
    }

    public PlantArchive updateArchive(Long id, PlantArchive archive) {
        archive.setId(id);
        return plantArchiveRepository.save(archive);
    }

    public void deleteArchive(Long id) {
        plantArchiveRepository.deleteById(id);
    }
}
