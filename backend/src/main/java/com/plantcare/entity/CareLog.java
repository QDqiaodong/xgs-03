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
@Table(name = "care_log")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class CareLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plant_archive_id", nullable = false)
    private Long plantArchiveId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    @Column(name = "operation_type", nullable = false, length = 50)
    private String operationType;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(name = "growth_status", length = 200)
    private String growthStatus;

    @Column(name = "disease_status", length = 200)
    private String diseaseStatus;

    @Column(name = "image_urls", columnDefinition = "TEXT")
    private String imageUrls;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
