package com.lucas.msla.mapper;

import com.lucas.msla.dto.ConversionHistoryResponseDTO;
import com.lucas.msla.dto.ConvertResponseDTO;
import com.lucas.msla.entity.ConversionHistory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ConversionMapper {

    public ConvertResponseDTO toConvertResponseDTO(ConversionHistory entity, LocalDate exchangeRateDate) {
        return ConvertResponseDTO.builder()
                .from(entity.getSourceCurrency())
                .to(entity.getTargetCurrency())
                .amount(entity.getAmount())
                .exchangeRate(entity.getExchangeRate())
                .convertedAmount(entity.getConvertedAmount())
                .date(exchangeRateDate)
                .timestamp(entity.getConversionDate())
                .success(entity.getSuccess())
                .build();
    }

    public ConversionHistoryResponseDTO toHistoryResponseDTO(ConversionHistory entity) {
        return ConversionHistoryResponseDTO.builder()
                .id(entity.getId())
                .from(entity.getSourceCurrency())
                .to(entity.getTargetCurrency())
                .amount(entity.getAmount())
                .exchangeRate(entity.getExchangeRate())
                .convertedAmount(entity.getConvertedAmount())
                .conversionDate(entity.getConversionDate())
                .build();
    }
}
