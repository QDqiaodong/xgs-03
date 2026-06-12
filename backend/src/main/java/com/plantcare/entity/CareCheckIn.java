package com.plantcare.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "care_checkin", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "checkin_date"})
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class CareCheckIn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "checkin_date", nullable = false)
    private LocalDate checkinDate;

    @Column(name = "care_log_ids", columnDefinition = "TEXT")
    private String careLogIds;

    @Column(name = "care_count", nullable = false)
    private Integer careCount;

    @Column(length = 500)
    private String remark;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
