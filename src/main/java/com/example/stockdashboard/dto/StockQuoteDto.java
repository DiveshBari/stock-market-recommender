package com.example.stockdashboard.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class StockQuoteDto {
    private String symbol;
    private BigDecimal currentPrice;
    private long currentVolume;
    private List<BigDecimal> closePrices;
}
