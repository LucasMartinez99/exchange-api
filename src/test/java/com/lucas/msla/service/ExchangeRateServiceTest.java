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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExchangeRateServiceTest {

    @Mock private ExchangeRateClient exchangeRateClient;
    @Mock private ConversionHistoryRepository conversionHistoryRepository;
    @Mock private ConversionMapper conversionMapper;

    private ExchangeRateService exchangeRateService;

    @BeforeEach
    void setUp() {
        exchangeRateService = new ExchangeRateService(
                exchangeRateClient,
                conversionHistoryRepository,
                conversionMapper,
                "fake-api-key"
        );
    }

    @Test
    void shouldConvertCurrencyAndSaveHistory() {
        ConvertRequestDTO request = ConvertRequestDTO.builder()
                .from("USD")
                .to("PEN")
                .amount(BigDecimal.valueOf(10))
                .build();

        ExchangeRateApiResponse apiResponse = new ExchangeRateApiResponse(
                true,
                new ExchangeRateApiResponse.RateInfo(BigDecimal.valueOf(3.426126)),
                BigDecimal.valueOf(34.26126),
                "2026-03-07"
        );

        ConversionHistory savedEntity = ConversionHistory.builder()
                .id(1L)
                .sourceCurrency("USD")
                .targetCurrency("PEN")
                .amount(BigDecimal.valueOf(10))
                .exchangeRate(BigDecimal.valueOf(3.426126))
                .convertedAmount(BigDecimal.valueOf(34.26126))
                .conversionDate(LocalDateTime.now())
                .success(true)
                .build();

        ConvertResponseDTO responseDTO = ConvertResponseDTO.builder()
                .from("USD")
                .to("PEN")
                .amount(BigDecimal.valueOf(10))
                .exchangeRate(BigDecimal.valueOf(3.426126))
                .convertedAmount(BigDecimal.valueOf(34.26126))
                .date(LocalDate.parse("2026-03-07"))
                .timestamp(savedEntity.getConversionDate())
                .success(true)
                .build();

        when(exchangeRateClient.convertCurrency("PEN", "USD", BigDecimal.valueOf(10), "fake-api-key"))
                .thenReturn(apiResponse);
        when(conversionHistoryRepository.save(any(ConversionHistory.class))).thenReturn(savedEntity);
        when(conversionMapper.toConvertResponseDTO(eq(savedEntity), eq(LocalDate.parse("2026-03-07"))))
                .thenReturn(responseDTO);

        ConvertResponseDTO result = exchangeRateService.convertCurrency(request);

        assertNotNull(result);
        assertEquals("USD", result.getFrom());
        assertEquals("PEN", result.getTo());
        assertEquals(BigDecimal.valueOf(10), result.getAmount());
        assertTrue(result.getSuccess());

        verify(conversionHistoryRepository).save(any(ConversionHistory.class));
        verify(conversionMapper).toConvertResponseDTO(eq(savedEntity), eq(LocalDate.parse("2026-03-07")));
    }

    @Test
    void shouldThrowExternalApiExceptionWhenConversionFails() {
        ConvertRequestDTO request = ConvertRequestDTO.builder()
                .from("USD")
                .to("PEN")
                .amount(BigDecimal.valueOf(10))
                .build();

        ExchangeRateApiResponse failedResponse = new ExchangeRateApiResponse(false, null, null, null);

        when(exchangeRateClient.convertCurrency(any(), any(), any(), any()))
                .thenReturn(failedResponse);

        assertThrows(ExternalApiException.class, () -> exchangeRateService.convertCurrency(request));
        verify(conversionHistoryRepository, never()).save(any());
    }

    @Test
    void shouldReturnConversionHistory() {
        LocalDate startDate = LocalDate.of(2026, 3, 1);
        LocalDate endDate = LocalDate.of(2026, 3, 31);

        ConversionHistory entity = ConversionHistory.builder()
                .id(1L)
                .sourceCurrency("USD")
                .targetCurrency("PEN")
                .amount(BigDecimal.valueOf(10))
                .exchangeRate(BigDecimal.valueOf(3.43))
                .convertedAmount(BigDecimal.valueOf(34.30))
                .conversionDate(LocalDateTime.of(2026, 3, 7, 10, 30))
                .success(true)
                .build();

        ConversionHistoryResponseDTO dto = ConversionHistoryResponseDTO.builder()
                .id(1L)
                .from("USD")
                .to("PEN")
                .amount(BigDecimal.valueOf(10))
                .conversionDate(LocalDateTime.of(2026, 3, 7, 10, 30))
                .build();

        when(conversionHistoryRepository.findByConversionDateBetween(
                startDate.atStartOfDay(),
                endDate.atTime(LocalTime.MAX)
        )).thenReturn(List.of(entity));
        when(conversionMapper.toHistoryResponseDTO(entity)).thenReturn(dto);

        List<ConversionHistoryResponseDTO> result = exchangeRateService.getHistory(startDate, endDate);

        assertEquals(1, result.size());
        assertEquals("USD", result.get(0).getFrom());
        verify(conversionHistoryRepository).findByConversionDateBetween(
                startDate.atStartOfDay(),
                endDate.atTime(LocalTime.MAX)
        );
    }

    @Test
    void shouldReturnSummaryByCurrency() {
        when(conversionHistoryRepository.sumConvertedAmountByTargetCurrency("PEN"))
                .thenReturn(BigDecimal.valueOf(100.50));

        SummaryResponseDTO result = exchangeRateService.getSummaryByCurrency("PEN");

        assertEquals("PEN", result.getCurrency());
        assertEquals(BigDecimal.valueOf(100.50), result.getTotalConvertedAmount());
        verify(conversionHistoryRepository).sumConvertedAmountByTargetCurrency("PEN");
    }
}
