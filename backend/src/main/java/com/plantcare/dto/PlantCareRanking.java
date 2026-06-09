package com.plantcare.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlantCareRanking {

    private Long plantArchiveId;

    private String customName;

    private String imageUrl;

    private Long careCount;
}
