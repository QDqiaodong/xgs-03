package com.plantcare.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "care_reminder")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class CareReminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "plant_archive_id", nullable = false)
    private Long plantArchiveId;

    @Column(name = "reminder_type", nullable = false, length = 50)
    private String reminderType;

    @Column(name = "reminder_date", nullable = false)
    private LocalDate reminderDate;

    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "deferred_count")
    private Integer deferredCount = 0;

    @Column(name = "care_log_id")
    private Long careLogId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plant_archive_id", insertable = false, updatable = false)
    private PlantArchive plantArchive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "care_log_id", insertable = false, updatable = false)
    private CareLog careLog;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
