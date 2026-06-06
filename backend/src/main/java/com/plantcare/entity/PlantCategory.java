package com.plantcare.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "plant_category")
public class PlantCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "scientific_name", length = 200)
    private String scientificName;

    @Column(length = 50)
    private String category;

    @Column(name = "light_requirement", length = 100)
    private String lightRequirement;

    @Column(name = "water_requirement", length = 100)
    private String waterRequirement;

    @Column(name = "temperature_range", length = 100)
    private String temperatureRange;

    @Column(length = 100)
    private String humidity;

    @Column(length = 200)
    private String fertilization;

    @Column(length = 200)
    private String pruning;

    @Column(name = "common_diseases", columnDefinition = "TEXT")
    private String commonDiseases;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
