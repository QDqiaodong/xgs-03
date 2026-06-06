package com.plantcare.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "plant_archive")
public class PlantArchive {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "plant_category_id")
    private Long plantCategoryId;

    @Column(name = "custom_name", length = 100)
    private String customName;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(length = 200)
    private String location;

    @Column(length = 100)
    private String environment;

    @Column(name = "reminder_enabled")
    private Boolean reminderEnabled = false;

    @Column(name = "water_interval")
    private Integer waterInterval;

    @Column(name = "fertilize_interval")
    private Integer fertilizeInterval;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plant_category_id", insertable = false, updatable = false)
    private PlantCategory plantCategory;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
