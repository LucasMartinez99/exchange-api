package com.lucas.msla.repository;

import com.lucas.msla.entity.ConversionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface ConversionHistoryRepository extends JpaRepository<ConversionHistory, Long> {

    List<ConversionHistory> findByConversionDateBetween(LocalDateTime start, LocalDateTime end);

    @Query("""
            SELECT COALESCE(SUM(c.convertedAmount), 0)
            FROM ConversionHistory c
            WHERE c.targetCurrency = :currency
            """)
    BigDecimal sumConvertedAmountByTargetCurrency(@Param("currency") String currency);
}
