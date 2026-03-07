package com.lucas.msla.controller;

import com.lucas.msla.dto.ConversionHistoryResponseDTO;
import com.lucas.msla.dto.ConvertRequestDTO;
import com.lucas.msla.dto.ConvertResponseDTO;
import com.lucas.msla.dto.SummaryResponseDTO;
import com.lucas.msla.service.ExchangeRateService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/exchange")
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    public ExchangeRateController(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    @PostMapping("/convert")
    public ResponseEntity<ConvertResponseDTO> convertCurrency(@Valid @RequestBody ConvertRequestDTO request) {
        return ResponseEntity.ok(exchangeRateService.convertCurrency(request));
    }

    @GetMapping("/history")
    public ResponseEntity<List<ConversionHistoryResponseDTO>> getHistory(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {
        return ResponseEntity.ok(exchangeRateService.getHistory(startDate, endDate));
    }

    @GetMapping("/summary")
    public ResponseEntity<SummaryResponseDTO> getSummaryByCurrency(@RequestParam String currency) {
        return ResponseEntity.ok(exchangeRateService.getSummaryByCurrency(currency));
    }
}