package com.lucas.msla.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConvertResponseDTO {

    private String from;
    private String to;
    private BigDecimal amount;
    private BigDecimal exchangeRate;
    private BigDecimal convertedAmount;
    private LocalDate date;
    private Boolean success;
}