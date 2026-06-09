package com.plantcare.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CareStatisticsDTO {

    private Long userId;

    private LocalDate startDate;

    private LocalDate endDate;

    private Long totalCareCount;

    private Integer consecutiveCareDays;

    private List<OperationTypeStats> operationTypeStats;

    private PlantCareRanking mostFrequentPlant;

    private List<MonthlyTrendItem> monthlyTrend;

    private List<CategoryCareDistribution> categoryDistribution;
}
