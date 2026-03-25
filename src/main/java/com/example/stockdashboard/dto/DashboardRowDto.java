package com.example.stockdashboard.dto;

import com.example.stockdashboard.domain.Prediction;
import com.example.stockdashboard.domain.Trend;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DashboardRowDto {
    private String symbol;
    private BigDecimal price;
    private Trend trend;
    private Integer sentimentScore;
    private Prediction prediction;
}
