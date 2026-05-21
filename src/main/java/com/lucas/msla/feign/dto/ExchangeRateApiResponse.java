package com.lucas.msla.feign.dto;

import java.math.BigDecimal;

public record ExchangeRateApiResponse(
        Boolean success,
        RateInfo info,
        BigDecimal result,
        String date
) {
    public record RateInfo(BigDecimal rate) {}
}
