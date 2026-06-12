package com.plantcare.service;

import com.plantcare.entity.PlantPhoto;
import com.plantcare.repository.PlantPhotoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlantPhotoService {

    private final PlantPhotoRepository plantPhotoRepository;

    private static final String UPLOAD_DIR = "uploads/plant-photos";

    public List<PlantPhoto> getPhotosByArchiveId(Long plantArchiveId) {
        try {
            return plantPhotoRepository.findByPlantArchiveIdOrderByPhotoDateDesc(plantArchiveId);
        } catch (Exception e) {
            log.error("getPhotosByArchiveId failed for plantArchiveId={}", plantArchiveId, e);
            throw e;
        }
    }

    public Optional<PlantPhoto> getPhotoById(Long id) {
        try {
            return plantPhotoRepository.findById(id);
        } catch (Exception e) {
            log.error("getPhotoById failed for id={}", id, e);
            throw e;
        }
    }

    @Transactional
    public PlantPhoto uploadPhoto(Long plantArchiveId, Long userId, MultipartFile file, String description, LocalDate photoDate) {
        try {
            String imageUrl = saveFile(file);

            PlantPhoto photo = new PlantPhoto();
            photo.setPlantArchiveId(plantArchiveId);
            photo.setUserId(userId);
            photo.setImageUrl(imageUrl);
            photo.setDescription(description);
            photo.setPhotoDate(photoDate != null ? photoDate : LocalDate.now());
            photo.setIsCover(false);

            long count = plantPhotoRepository.countByPlantArchiveId(plantArchiveId);
            if (count == 0) {
                photo.setIsCover(true);
            }

            return plantPhotoRepository.save(photo);
        } catch (Exception e) {
            log.error("uploadPhoto failed for plantArchiveId={}", plantArchiveId, e);
            throw e;
        }
    }

    @Transactional
    public PlantPhoto updatePhoto(Long id, String description, LocalDate photoDate) {
        try {
            PlantPhoto photo = plantPhotoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Photo not found"));
            if (description != null) {
                photo.setDescription(description);
            }
            if (photoDate != null) {
                photo.setPhotoDate(photoDate);
            }
            return plantPhotoRepository.save(photo);
        } catch (Exception e) {
            log.error("updatePhoto failed for id={}", id, e);
            throw e;
        }
    }

    @Transactional
    public void deletePhoto(Long id) {
        try {
            PlantPhoto photo = plantPhotoRepository.findById(id).orElse(null);
            if (photo != null) {
                deleteFile(photo.getImageUrl());
                boolean wasCover = Boolean.TRUE.equals(photo.getIsCover());
                Long archiveId = photo.getPlantArchiveId();
                plantPhotoRepository.deleteById(id);

                if (wasCover) {
                    List<PlantPhoto> remaining = plantPhotoRepository.findByPlantArchiveIdOrderByPhotoDateDesc(archiveId);
                    if (!remaining.isEmpty()) {
                        PlantPhoto newCover = remaining.get(0);
                        newCover.setIsCover(true);
                        plantPhotoRepository.save(newCover);
                    }
                }
            }
        } catch (Exception e) {
            log.error("deletePhoto failed for id={}", id, e);
            throw e;
        }
    }

    @Transactional
    public PlantPhoto setCover(Long id) {
        try {
            PlantPhoto photo = plantPhotoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Photo not found"));
            plantPhotoRepository.clearCoverByPlantArchiveId(photo.getPlantArchiveId());
            photo.setIsCover(true);
            return plantPhotoRepository.save(photo);
        } catch (Exception e) {
            log.error("setCover failed for id={}", id, e);
            throw e;
        }
    }

    public Optional<PlantPhoto> getCoverPhoto(Long plantArchiveId) {
        try {
            return plantPhotoRepository.findCoverByPlantArchiveId(plantArchiveId);
        } catch (Exception e) {
            log.error("getCoverPhoto failed for plantArchiveId={}", plantArchiveId, e);
            throw e;
        }
    }

    private String saveFile(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";

        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        Path datePath = uploadPath.resolve(dateDir);
        if (!Files.exists(datePath)) {
            Files.createDirectories(datePath);
        }

        String filename = UUID.randomUUID().toString().replace("-", "") + extension;
        Path filePath = datePath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return "/uploads/plant-photos/" + dateDir + "/" + filename;
    }

    private void deleteFile(String imageUrl) {
        if (imageUrl == null || !imageUrl.startsWith("/uploads/")) return;
        try {
            Path filePath = Paths.get(imageUrl.substring(1));
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("Failed to delete file: {}", imageUrl, e);
        }
    }
}
