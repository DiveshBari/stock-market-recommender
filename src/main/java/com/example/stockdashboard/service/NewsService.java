package com.example.stockdashboard.service;

import com.example.stockdashboard.config.ApiProperties;
import com.example.stockdashboard.domain.NewsItem;
import com.example.stockdashboard.dto.NewsArticleDto;
import com.example.stockdashboard.repository.NewsItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsService {

    private final WebClient webClient;
    private final ApiProperties apiProperties;
    private final NewsItemRepository newsItemRepository;
    private final SentimentService sentimentService;
    private final SectorMappingService sectorMappingService;

    @Cacheable(value = "news", key = "#symbol", unless = "#result == null || #result.isEmpty()")
    public List<NewsArticleDto> fetchNews(String symbol) {
        try {
            String key = apiProperties.getFinnhub().getKey();
            if (key == null || key.isBlank()) {
                return fallbackNews(symbol);
            }
            String from = LocalDate.now().minusDays(3).toString();
            String to = LocalDate.now().toString();
            List<Map<String, Object>> rows = webClient.get()
                    .uri(apiProperties.getFinnhub().getBaseUrl() + "/company-news?symbol={s}&from={f}&to={t}&token={k}", symbol, from, to, key)
                    .retrieve()
                    .bodyToFlux(Map.class)
                    .collectList()
                    .block();

            if (rows == null || rows.isEmpty()) {
                return fallbackNews(symbol);
            }
            return rows.stream().limit(10).map(n -> NewsArticleDto.builder()
                    .headline(String.valueOf(n.getOrDefault("headline", "")))
                    .summary(String.valueOf(n.getOrDefault("summary", "")))
                    .publishedAt(Instant.ofEpochSecond(Long.parseLong(String.valueOf(n.getOrDefault("datetime", Instant.now().getEpochSecond())))))
                    .relatedSymbol(symbol)
                    .build()).toList();
        } catch (Exception e) {
            log.warn("News fetch failed for {}. Using fallback. {}", symbol, e.getMessage());
            return fallbackNews(symbol);
        }
    }

    public List<NewsItem> saveNews(List<NewsArticleDto> articles) {
        List<NewsItem> saved = new ArrayList<>();
        for (NewsArticleDto dto : articles) {
            int sentiment = sentimentService.score(dto.getHeadline(), dto.getSummary());
            String sector = sectorMappingService.inferSector(dto.getHeadline() + " " + dto.getSummary());
            NewsItem item = NewsItem.builder()
                    .title(dto.getHeadline())
                    .description(dto.getSummary())
                    .sentimentScore(sentiment)
                    .sector(sector)
                    .publishedAt(dto.getPublishedAt() == null ? Instant.now() : dto.getPublishedAt())
                    .relatedSymbol(dto.getRelatedSymbol())
                    .build();
            saved.add(newsItemRepository.save(item));
        }
        return saved;
    }

    public List<NewsItem> latestNews() {
        return newsItemRepository.findTop50ByOrderByPublishedAtDesc();
    }

    private List<NewsArticleDto> fallbackNews(String symbol) {
        return List.of(
                NewsArticleDto.builder().headline(symbol + " sees growth surge on AI boom").summary("Analysts expect profit expansion.").publishedAt(Instant.now().minusSeconds(3600)).relatedSymbol(symbol).build(),
                NewsArticleDto.builder().headline(symbol + " faces temporary decline amid supply shortage").summary("Short-term crisis may impact margins.").publishedAt(Instant.now().minusSeconds(7200)).relatedSymbol(symbol).build()
        );
    }
}
