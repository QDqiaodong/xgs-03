package com.plantcare.controller;

import com.plantcare.entity.Tag;
import com.plantcare.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/tags", produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Tag>> getUserTags(@PathVariable Long userId) {
        return ResponseEntity.ok(tagService.getUserTags(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tag> getTagById(@PathVariable Long id) {
        return tagService.getTagById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createTag(@RequestBody Tag tag) {
        try {
            return ResponseEntity.ok(tagService.createTag(tag));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTag(@PathVariable Long id, @RequestBody Tag tag) {
        try {
            return ResponseEntity.ok(tagService.updateTag(id, tag));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/rename")
    public ResponseEntity<?> renameTag(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String newName = request.get("name");
            if (newName == null || newName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "标签名称不能为空"));
            }
            return ResponseEntity.ok(tagService.renameTag(id, newName.trim()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTag(@PathVariable Long id) {
        try {
            tagService.deleteTag(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/order")
    public ResponseEntity<Void> updateTagOrder(@RequestParam Long userId, @RequestBody List<Long> tagIds) {
        tagService.updateTagOrder(userId, tagIds);
        return ResponseEntity.ok().build();
    }
}
