package com.plantcare.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckInCalendarDTO {

    private Long userId;

    private Integer year;

    private Integer month;

    private List<LocalDate> checkInDates;

    private Integer monthCheckInCount;
}
