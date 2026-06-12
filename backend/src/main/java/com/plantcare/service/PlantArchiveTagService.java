package com.plantcare.service;

import com.plantcare.entity.PlantArchive;
import com.plantcare.entity.PlantArchiveTag;
import com.plantcare.entity.Tag;
import com.plantcare.repository.PlantArchiveRepository;
import com.plantcare.repository.PlantArchiveTagRepository;
import com.plantcare.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlantArchiveTagService {

    private final PlantArchiveTagRepository plantArchiveTagRepository;
    private final PlantArchiveRepository plantArchiveRepository;
    private final TagRepository tagRepository;

    public List<Tag> getTagsForPlantArchive(Long plantArchiveId) {
        try {
            List<PlantArchiveTag> relations = plantArchiveTagRepository.findByPlantArchiveId(plantArchiveId);
            return relations.stream()
                    .map(PlantArchiveTag::getTag)
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparing(Tag::getSortOrder, Comparator.nullsLast(Integer::compareTo)))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("getTagsForPlantArchive failed for plantArchiveId={}", plantArchiveId, e);
            throw e;
        }
    }

    public Map<Long, List<Tag>> getTagsForPlantArchives(List<Long> plantArchiveIds) {
        try {
            List<PlantArchiveTag> relations = plantArchiveTagRepository.findByPlantArchiveIdIn(plantArchiveIds);
            Map<Long, List<Tag>> result = new HashMap<>();
            for (PlantArchiveTag relation : relations) {
                Long plantId = relation.getPlantArchiveId();
                Tag tag = relation.getTag();
                if (tag != null) {
                    result.computeIfAbsent(plantId, k -> new ArrayList<>()).add(tag);
                }
            }
            for (Map.Entry<Long, List<Tag>> entry : result.entrySet()) {
                entry.setValue(entry.getValue().stream()
                        .sorted(Comparator.comparing(Tag::getSortOrder, Comparator.nullsLast(Integer::compareTo)))
                        .collect(Collectors.toList()));
            }
            return result;
        } catch (Exception e) {
            log.error("getTagsForPlantArchives failed", e);
            throw e;
        }
    }

    @Transactional
    public PlantArchiveTag addTagToPlantArchive(Long plantArchiveId, Long tagId) {
        try {
            Optional<PlantArchiveTag> existing = plantArchiveTagRepository.findByPlantArchiveIdAndTagId(plantArchiveId, tagId);
            if (existing.isPresent()) {
                return existing.get();
            }

            if (!plantArchiveRepository.existsById(plantArchiveId)) {
                throw new IllegalArgumentException("植物档案不存在");
            }
            if (!tagRepository.existsById(tagId)) {
                throw new IllegalArgumentException("标签不存在");
            }

            PlantArchiveTag relation = new PlantArchiveTag();
            relation.setPlantArchiveId(plantArchiveId);
            relation.setTagId(tagId);
            return plantArchiveTagRepository.save(relation);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("addTagToPlantArchive failed for plantArchiveId={}, tagId={}", plantArchiveId, tagId, e);
            throw e;
        }
    }

    @Transactional
    public List<Tag> setTagsForPlantArchive(Long plantArchiveId, List<Long> tagIds) {
        try {
            plantArchiveTagRepository.deleteByPlantArchiveId(plantArchiveId);

            List<Long> safeTagIds = tagIds == null ? new ArrayList<>() : tagIds;
            for (Long tagId : new HashSet<>(safeTagIds)) {
                PlantArchiveTag relation = new PlantArchiveTag();
                relation.setPlantArchiveId(plantArchiveId);
                relation.setTagId(tagId);
                plantArchiveTagRepository.save(relation);
            }

            return getTagsForPlantArchive(plantArchiveId);
        } catch (Exception e) {
            log.error("setTagsForPlantArchive failed for plantArchiveId={}", plantArchiveId, e);
            throw e;
        }
    }

    @Transactional
    public void removeTagFromPlantArchive(Long plantArchiveId, Long tagId) {
        try {
            plantArchiveTagRepository.deleteByPlantArchiveIdAndTagId(plantArchiveId, tagId);
        } catch (Exception e) {
            log.error("removeTagFromPlantArchive failed for plantArchiveId={}, tagId={}", plantArchiveId, tagId, e);
            throw e;
        }
    }

    public List<PlantArchive> getPlantArchivesByTag(Long userId, Long tagId) {
        try {
            List<Long> plantArchiveIds = plantArchiveTagRepository.findPlantArchiveIdsByTagId(tagId);
            if (plantArchiveIds.isEmpty()) {
                return Collections.emptyList();
            }
            List<PlantArchive> allArchives = plantArchiveRepository.findByUserIdOrderByCreatedAtDesc(userId);
            Set<Long> taggedIds = new HashSet<>(plantArchiveIds);
            return allArchives.stream()
                    .filter(archive -> taggedIds.contains(archive.getId()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("getPlantArchivesByTag failed for userId={}, tagId={}", userId, tagId, e);
            throw e;
        }
    }

    public List<PlantArchive> getPlantArchivesByTags(Long userId, List<Long> tagIds) {
        try {
            if (tagIds == null || tagIds.isEmpty()) {
                return plantArchiveRepository.findByUserIdOrderByCreatedAtDesc(userId);
            }
            List<Long> plantArchiveIds = plantArchiveTagRepository.findPlantArchiveIdsByTagIdIn(tagIds);
            if (plantArchiveIds.isEmpty()) {
                return Collections.emptyList();
            }
            List<PlantArchive> allArchives = plantArchiveRepository.findByUserIdOrderByCreatedAtDesc(userId);
            Set<Long> taggedIds = new HashSet<>(plantArchiveIds);
            return allArchives.stream()
                    .filter(archive -> taggedIds.contains(archive.getId()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("getPlantArchivesByTags failed for userId={}", userId, e);
            throw e;
        }
    }

    public Long getPlantCountForTag(Long tagId) {
        try {
            return plantArchiveTagRepository.countByTagId(tagId);
        } catch (Exception e) {
            log.error("getPlantCountForTag failed for tagId={}", tagId, e);
            throw e;
        }
    }

    public Map<Long, Long> getPlantCountsForTags(List<Long> tagIds) {
        try {
            Map<Long, Long> result = new HashMap<>();
            for (Long tagId : tagIds) {
                result.put(tagId, plantArchiveTagRepository.countByTagId(tagId));
            }
            return result;
        } catch (Exception e) {
            log.error("getPlantCountsForTags failed", e);
            throw e;
        }
    }
}
