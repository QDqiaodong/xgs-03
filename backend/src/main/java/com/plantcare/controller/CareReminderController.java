package com.plantcare.controller;

import com.plantcare.dto.CompleteReminderRequest;
import com.plantcare.dto.DeferReminderRequest;
import com.plantcare.entity.CareReminder;
import com.plantcare.service.CareReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/care-reminders", produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
public class CareReminderController {

    private final CareReminderService careReminderService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CareReminder>> getUserReminders(@PathVariable Long userId) {
        return ResponseEntity.ok(careReminderService.getUserReminders(userId));
    }

    @GetMapping("/user/{userId}/paged")
    public ResponseEntity<Page<CareReminder>> getUserRemindersPaged(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(careReminderService.getUserRemindersPaged(userId, page, size));
    }

    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<List<CareReminder>> getUserRemindersByStatus(
            @PathVariable Long userId,
            @PathVariable String status) {
        return ResponseEntity.ok(careReminderService.getUserRemindersByStatus(userId, status));
    }

    @GetMapping("/user/{userId}/status/{status}/paged")
    public ResponseEntity<Page<CareReminder>> getUserRemindersByStatusPaged(
            @PathVariable Long userId,
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(careReminderService.getUserRemindersByStatusPaged(userId, status, page, size));
    }

    @GetMapping("/user/{userId}/pending")
    public ResponseEntity<List<CareReminder>> getPendingReminders(@PathVariable Long userId) {
        return ResponseEntity.ok(careReminderService.getPendingReminders(userId));
    }

    @GetMapping("/plant/{plantArchiveId}")
    public ResponseEntity<List<CareReminder>> getPlantReminders(@PathVariable Long plantArchiveId) {
        return ResponseEntity.ok(careReminderService.getPlantReminders(plantArchiveId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CareReminder> getReminderById(@PathVariable Long id) {
        return careReminderService.getReminderById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<CareReminder> completeReminder(
            @PathVariable Long id,
            @RequestBody(required = false) CompleteReminderRequest request) {
        return careReminderService.completeReminder(id, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/defer")
    public ResponseEntity<CareReminder> deferReminder(
            @PathVariable Long id,
            @RequestBody(required = false) DeferReminderRequest request) {
        return careReminderService.deferReminder(id, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<CareReminder> cancelReminder(@PathVariable Long id) {
        return careReminderService.cancelReminder(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/trigger-scan")
    public ResponseEntity<Void> triggerScan() {
        careReminderService.triggerScan();
        return ResponseEntity.ok().build();
    }
}
