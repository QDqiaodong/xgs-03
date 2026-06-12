package com.plantcare.controller;

import com.plantcare.dto.CheckInCalendarDTO;
import com.plantcare.dto.CheckInRequestDTO;
import com.plantcare.dto.CheckInStatusDTO;
import com.plantcare.entity.AchievementBadge;
import com.plantcare.entity.CareCheckIn;
import com.plantcare.service.CheckInService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/checkin", produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
public class CheckInController {

    private final CheckInService checkInService;

    @PostMapping
    public ResponseEntity<CareCheckIn> checkIn(@RequestBody CheckInRequestDTO request) {
        return ResponseEntity.ok(checkInService.checkIn(request));
    }

    @GetMapping("/status/{userId}")
    public ResponseEntity<CheckInStatusDTO> getCheckInStatus(@PathVariable Long userId) {
        return ResponseEntity.ok(checkInService.getCheckInStatus(userId));
    }

    @GetMapping("/calendar/{userId}")
    public ResponseEntity<CheckInCalendarDTO> getCheckInCalendar(
            @PathVariable Long userId,
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(checkInService.getCheckInCalendar(userId, year, month));
    }

    @GetMapping("/history/{userId}")
    public ResponseEntity<List<CareCheckIn>> getUserCheckIns(@PathVariable Long userId) {
        return ResponseEntity.ok(checkInService.getUserCheckIns(userId));
    }

    @GetMapping("/achievements/{userId}")
    public ResponseEntity<List<AchievementBadge>> getUserAchievements(@PathVariable Long userId) {
        return ResponseEntity.ok(checkInService.getUserAchievements(userId));
    }
}
