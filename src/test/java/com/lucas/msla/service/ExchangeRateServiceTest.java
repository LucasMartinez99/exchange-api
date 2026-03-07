package com.lucas.msla.service;

import com.lucas.msla.dto.ConvertRequestDTO;
import com.lucas.msla.dto.ConvertResponseDTO;
import com.lucas.msla.entity.ConversionHistory;
import com.lucas.msla.feign.ExchangeRateClient;
import com.lucas.msla.mapper.ConversionMapper;
import com.lucas.msla.repository.ConversionHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExchangeRateServiceTest {

    @Mock
    private ExchangeRateClient exchangeRateClient;

    @Mock
    private ConversionHistoryRepository conversionHistoryRepository;

    @Mock
    private ConversionMapper conversionMapper;

    @InjectMocks
    private ExchangeRateService exchangeRateService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(exchangeRateService, "apiKey", "fake-api-key");
    }

    @Test
    void shouldConvertCurrencyAndSaveHistory() {
        ConvertRequestDTO request = ConvertRequestDTO.builder()
                .from("USD")
                .to("PEN")
                .amount(BigDecimal.valueOf(10))
                .build();

        Map<String, Object> info = new HashMap<>();
        info.put("rate", 3.426126);

        Map<String, Object> externalResponse = new HashMap<>();
        externalResponse.put("success", true);
        externalResponse.put("info", info);
        externalResponse.put("result", 34.26126);
        externalResponse.put("date", "2026-03-07");

        ConversionHistory savedEntity = ConversionHistory.builder()
                .id(1L)
                .sourceCurrency("USD")
                .targetCurrency("PEN")
                .amount(BigDecimal.valueOf(10))
                .exchangeRate(BigDecimal.valueOf(3.426126))
                .convertedAmount(BigDecimal.valueOf(34.26126))
                .conversionDate(LocalDate.parse("2026-03-07"))
                .success(true)
                .build();

        ConvertResponseDTO responseDTO = ConvertResponseDTO.builder()
                .from("USD")
                .to("PEN")
                .amount(BigDecimal.valueOf(10))
                .exchangeRate(BigDecimal.valueOf(3.426126))
                .convertedAmount(BigDecimal.valueOf(34.26126))
                .date(LocalDate.parse("2026-03-07"))
                .success(true)
                .build();

        when(exchangeRateClient.convertCurrency(
                eq("PEN"),
                eq("USD"),
                eq(BigDecimal.valueOf(10)),
                eq("fake-api-key")
        )).thenReturn(externalResponse);

        when(conversionHistoryRepository.save(any(ConversionHistory.class)))
                .thenReturn(savedEntity);

        when(conversionMapper.toConvertResponseDTO(savedEntity))
                .thenReturn(responseDTO);

        ConvertResponseDTO result = exchangeRateService.convertCurrency(request);

        assertNotNull(result);
        assertEquals("USD", result.getFrom());
        assertEquals("PEN", result.getTo());
        assertEquals(BigDecimal.valueOf(10), result.getAmount());
        assertEquals(BigDecimal.valueOf(3.426126), result.getExchangeRate());
        assertEquals(BigDecimal.valueOf(34.26126), result.getConvertedAmount());
        assertTrue(result.getSuccess());

        verify(exchangeRateClient, times(1))
                .convertCurrency("PEN", "USD", BigDecimal.valueOf(10), "fake-api-key");
        verify(conversionHistoryRepository, times(1))
                .save(any(ConversionHistory.class));
        verify(conversionMapper, times(1))
                .toConvertResponseDTO(savedEntity);
    }
}