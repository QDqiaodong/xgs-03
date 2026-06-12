package com.plantcare.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsDTO {

    private Long userId;

    private Integer totalCareDays;

    private Integer totalCareTimes;

    private Integer totalPlants;

    private Integer totalPosts;

    private Integer totalLikes;

    private Integer bestAnswerCount;
}
