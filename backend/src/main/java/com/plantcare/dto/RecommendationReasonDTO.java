package com.plantcare.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationReasonDTO {

    private String type;

    private String description;

    private Double score;
}
