package com.plantcare.service;

import com.plantcare.entity.Tag;
import com.plantcare.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    public List<Tag> getUserTags(Long userId) {
        try {
            return tagRepository.findByUserIdOrderBySortOrder(userId);
        } catch (Exception e) {
            log.error("getUserTags failed for userId={}", userId, e);
            throw e;
        }
    }

    public Optional<Tag> getTagById(Long id) {
        try {
            return tagRepository.findById(id);
        } catch (Exception e) {
            log.error("getTagById failed for id={}", id, e);
            throw e;
        }
    }

    @Transactional
    public Tag createTag(Tag tag) {
        try {
            Optional<Tag> existing = tagRepository.findByUserIdAndName(tag.getUserId(), tag.getName());
            if (existing.isPresent()) {
                throw new IllegalArgumentException("标签名称已存在");
            }
            if (tag.getColor() == null || tag.getColor().isEmpty()) {
                tag.setColor("#4caf50");
            }
            if (tag.getSortOrder() == null) {
                tag.setSortOrder(0);
            }
            return tagRepository.save(tag);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("createTag failed", e);
            throw e;
        }
    }

    @Transactional
    public Tag updateTag(Long id, Tag tag) {
        try {
            Tag existing = tagRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("标签不存在"));

            if (!existing.getUserId().equals(tag.getUserId())) {
                throw new IllegalArgumentException("无权修改此标签");
            }

            if (!existing.getName().equals(tag.getName())) {
                Optional<Tag> duplicate = tagRepository.findByUserIdAndName(tag.getUserId(), tag.getName());
                if (duplicate.isPresent()) {
                    throw new IllegalArgumentException("标签名称已存在");
                }
            }

            existing.setName(tag.getName());
            if (tag.getColor() != null) {
                existing.setColor(tag.getColor());
            }
            if (tag.getSortOrder() != null) {
                existing.setSortOrder(tag.getSortOrder());
            }

            return tagRepository.save(existing);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("updateTag failed for id={}", id, e);
            throw e;
        }
    }

    @Transactional
    public Tag renameTag(Long id, String newName) {
        try {
            Tag existing = tagRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("标签不存在"));

            Optional<Tag> duplicate = tagRepository.findByUserIdAndName(existing.getUserId(), newName);
            if (duplicate.isPresent() && !duplicate.get().getId().equals(id)) {
                throw new IllegalArgumentException("标签名称已存在");
            }

            existing.setName(newName);
            return tagRepository.save(existing);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("renameTag failed for id={}", id, e);
            throw e;
        }
    }

    @Transactional
    public void deleteTag(Long id) {
        try {
            if (!tagRepository.existsById(id)) {
                throw new IllegalArgumentException("标签不存在");
            }
            tagRepository.deleteById(id);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("deleteTag failed for id={}", id, e);
            throw e;
        }
    }

    @Transactional
    public void updateTagOrder(Long userId, List<Long> tagIds) {
        try {
            List<Tag> tags = tagRepository.findByUserIdAndIdIn(userId, tagIds);
            for (int i = 0; i < tagIds.size(); i++) {
                Long tagId = tagIds.get(i);
                for (Tag tag : tags) {
                    if (tag.getId().equals(tagId)) {
                        tag.setSortOrder(i);
                        tagRepository.save(tag);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            log.error("updateTagOrder failed for userId={}", userId, e);
            throw e;
        }
    }
}
