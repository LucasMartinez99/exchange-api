package com.lucas.msla.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "conversion_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sourceCurrency;

    @Column(nullable = false)
    private String targetCurrency;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal exchangeRate;

    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal convertedAmount;

    @Column(nullable = false)
    private LocalDate conversionDate;

    @Column(nullable = false)
    private Boolean success;
}