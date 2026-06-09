package com.plantcare.dto;

import lombok.Data;

@Data
public class CompleteReminderRequest {

    private String details;

    private String growthStatus;

    private String diseaseStatus;

    private String imageUrls;
}
