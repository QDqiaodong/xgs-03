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
@Table(name = "plant_photo")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PlantPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plant_archive_id", nullable = false)
    private Long plantArchiveId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "image_url", length = 500, nullable = false)
    private String imageUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "photo_date")
    private LocalDate photoDate;

    @Column(name = "is_cover")
    private Boolean isCover = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
