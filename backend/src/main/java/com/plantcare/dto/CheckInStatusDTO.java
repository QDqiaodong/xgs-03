package com.plantcare.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckInStatusDTO {

    private Long userId;

    private Boolean todayCheckedIn;

    private Integer currentStreak;

    private Integer maxStreak;

    private Integer totalCheckInDays;

    private LocalDate lastCheckInDate;

    private List<LocalDate> recentCheckInDates;

    private List<AchievementBadgeDTO> achievements;

    private List<AchievementBadgeDTO> lockedAchievements;
}
