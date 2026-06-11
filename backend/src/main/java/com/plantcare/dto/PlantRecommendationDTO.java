package com.plantcare.dto;

import com.plantcare.entity.PlantCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlantRecommendationDTO {

    private PlantCategory plantCategory;

    private Double totalScore;

    private String recommendationType;

    private List<RecommendationReasonDTO> reasons;
}
