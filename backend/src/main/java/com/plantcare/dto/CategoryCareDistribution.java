package com.plantcare.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryCareDistribution {

    private Long categoryId;

    private String categoryName;

    private Long careCount;

    private Double percentage;
}
