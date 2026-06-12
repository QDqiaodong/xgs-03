package com.plantcare.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AchievementBadgeDTO {

    private Long id;

    private String badgeType;

    private String badgeName;

    private String badgeIcon;

    private Integer streakDays;

    private String description;

    private Boolean unlocked;

    private LocalDateTime unlockedAt;
}
