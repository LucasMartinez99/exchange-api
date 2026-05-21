package com.lucas.exchangeapi.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
    private LocalDate date;          // exchange rate date from the external API
    private LocalDateTime timestamp; // when this conversion was recorded
    private Boolean success;
}
