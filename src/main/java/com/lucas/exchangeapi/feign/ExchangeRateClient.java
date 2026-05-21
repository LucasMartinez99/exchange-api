package com.lucas.exchangeapi.feign;

import com.lucas.exchangeapi.feign.dto.ExchangeRateApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@FeignClient(name = "exchangeRateClient", url = "${exchange.api.url}")
public interface ExchangeRateClient {

    @GetMapping("/convert")
    ExchangeRateApiResponse convertCurrency(
            @RequestParam("to") String to,
            @RequestParam("from") String from,
            @RequestParam("amount") BigDecimal amount,
            @RequestHeader("apikey") String apiKey
    );
}
