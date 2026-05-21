package com.lucas.msla.service;

import com.lucas.msla.dto.ConversionHistoryResponseDTO;
import com.lucas.msla.dto.ConvertRequestDTO;
import com.lucas.msla.dto.ConvertResponseDTO;
import com.lucas.msla.dto.SummaryResponseDTO;
import com.lucas.msla.entity.ConversionHistory;
import com.lucas.msla.exception.ExternalApiException;
import com.lucas.msla.feign.ExchangeRateClient;
import com.lucas.msla.feign.dto.ExchangeRateApiResponse;
import com.lucas.msla.mapper.ConversionMapper;
import com.lucas.msla.repository.ConversionHistoryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class ExchangeRateService {

    private final ExchangeRateClient exchangeRateClient;
    private final ConversionHistoryRepository conversionHistoryRepository;
    private final ConversionMapper conversionMapper;
    private final String apiKey;

    public ExchangeRateService(
            ExchangeRateClient exchangeRateClient,
            ConversionHistoryRepository conversionHistoryRepository,
            ConversionMapper conversionMapper,
            @Value("${exchange.api.key}") String apiKey) {
        this.exchangeRateClient = exchangeRateClient;
        this.conversionHistoryRepository = conversionHistoryRepository;
        this.conversionMapper = conversionMapper;
        this.apiKey = apiKey;
    }

    @Transactional
    public ConvertResponseDTO convertCurrency(ConvertRequestDTO request) {
        ExchangeRateApiResponse response = exchangeRateClient.convertCurrency(
                request.getTo(),
                request.getFrom(),
                request.getAmount(),
                apiKey
        );

        if (!Boolean.TRUE.equals(response.success())) {
            throw new ExternalApiException("Currency conversion failed in the external service");
        }

        ConversionHistory history = ConversionHistory.builder()
                .sourceCurrency(request.getFrom())
                .targetCurrency(request.getTo())
                .amount(request.getAmount())
                .exchangeRate(response.info().rate())
                .convertedAmount(response.result())
                .conversionDate(LocalDateTime.now())
                .success(true)
                .build();

        ConversionHistory saved = conversionHistoryRepository.save(history);
        LocalDate exchangeRateDate = LocalDate.parse(response.date());

        return conversionMapper.toConvertResponseDTO(saved, exchangeRateDate);
    }

    public List<ConversionHistoryResponseDTO> getHistory(LocalDate startDate, LocalDate endDate) {
        return conversionHistoryRepository
                .findByConversionDateBetween(startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX))
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
