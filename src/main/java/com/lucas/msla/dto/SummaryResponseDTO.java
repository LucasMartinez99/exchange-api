package com.lucas.msla.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SummaryResponseDTO {

    private String currency;
    private BigDecimal totalConvertedAmount;
}