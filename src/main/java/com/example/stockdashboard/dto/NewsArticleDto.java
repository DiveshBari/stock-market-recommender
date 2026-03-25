package com.example.stockdashboard.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class NewsArticleDto {
    private String headline;
    private String summary;
    private Instant publishedAt;
    private String relatedSymbol;
}
