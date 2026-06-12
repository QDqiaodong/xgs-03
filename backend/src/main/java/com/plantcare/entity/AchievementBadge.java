package com.plantcare.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "achievement_badge", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "badge_type"})
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class AchievementBadge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "badge_type", nullable = false, length = 50)
    private String badgeType;

    @Column(name = "badge_name", nullable = false, length = 100)
    private String badgeName;

    @Column(name = "badge_icon", length = 200)
    private String badgeIcon;

    @Column(name = "streak_days", nullable = false)
    private Integer streakDays;

    @Column(length = 500)
    private String description;

    @CreationTimestamp
    @Column(name = "unlocked_at", updatable = false)
    private LocalDateTime unlockedAt;
}
