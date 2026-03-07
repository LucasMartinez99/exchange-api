package com.lucas.msla.service;

import com.lucas.msla.dto.ConversionHistoryResponseDTO;
import com.lucas.msla.dto.ConvertRequestDTO;
import com.lucas.msla.dto.ConvertResponseDTO;
import com.lucas.msla.dto.SummaryResponseDTO;
import com.lucas.msla.entity.ConversionHistory;
import com.lucas.msla.feign.ExchangeRateClient;
import com.lucas.msla.mapper.ConversionMapper;
import com.lucas.msla.repository.ConversionHistoryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class ExchangeRateService {

    private final ExchangeRateClient exchangeRateClient;
    private final ConversionHistoryRepository conversionHistoryRepository;
    private final ConversionMapper conversionMapper;

    @Value("${exchange.api.key}")
    private String apiKey;

    public ExchangeRateService(ExchangeRateClient exchangeRateClient,
                               ConversionHistoryRepository conversionHistoryRepository,
                               ConversionMapper conversionMapper) {
        this.exchangeRateClient = exchangeRateClient;
        this.conversionHistoryRepository = conversionHistoryRepository;
        this.conversionMapper = conversionMapper;
    }

    public ConvertResponseDTO convertCurrency(ConvertRequestDTO request) {
        Map<String, Object> response = exchangeRateClient.convertCurrency(
                request.getTo(),
                request.getFrom(),
                request.getAmount(),
                apiKey
        );

        Boolean success = (Boolean) response.get("success");
        if (Boolean.FALSE.equals(success)) {
            throw new RuntimeException("La conversión falló en el servicio externo");
        }

        Map<String, Object> info = (Map<String, Object>) response.get("info");
        BigDecimal rate = new BigDecimal(info.get("rate").toString());
        BigDecimal result = new BigDecimal(response.get("result").toString());
        LocalDate date = LocalDate.parse(response.get("date").toString());

        ConversionHistory history = ConversionHistory.builder()
                .sourceCurrency(request.getFrom())
                .targetCurrency(request.getTo())
                .amount(request.getAmount())
                .exchangeRate(rate)
                .convertedAmount(result)
                .conversionDate(date)
                .success(true)
                .build();

        ConversionHistory saved = conversionHistoryRepository.save(history);

        return conversionMapper.toConvertResponseDTO(saved);
    }

    public List<ConversionHistoryResponseDTO> getHistory(LocalDate startDate, LocalDate endDate) {
        return conversionHistoryRepository.findByConversionDateBetween(startDate, endDate)
                .stream()
                .map(conversionMapper::toHistoryResponseDTO)
                .toList();
    }

    public SummaryResponseDTO getSummaryByCurrency(String currency) {
        BigDecimal total = conversionHistoryRepository.sumConvertedAmountByTargetCurrency(currency);

        return SummaryResponseDTO.builder()
                .currency(currency)
                .totalConvertedAmount(total)
                .build();
    }
}