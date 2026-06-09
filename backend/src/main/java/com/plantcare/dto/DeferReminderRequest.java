package com.plantcare.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DeferReminderRequest {

    private LocalDate newReminderDate;

    private Integer deferDays;
}
