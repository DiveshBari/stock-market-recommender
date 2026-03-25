package com.example.stockdashboard.service;

import com.example.stockdashboard.config.ApiProperties;
import com.example.stockdashboard.domain.StockData;
import com.example.stockdashboard.domain.Trend;
import com.example.stockdashboard.dto.StockQuoteDto;
import com.example.stockdashboard.repository.StockDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockService {

    private final WebClient webClient;
    private final ApiProperties apiProperties;
    private final StockDataRepository stockDataRepository;

    @Cacheable(value = "stockQuote", key = "#symbol", unless = "#result == null")
    public StockQuoteDto fetchStockQuote(String symbol) {
        try {
            String base = apiProperties.getFinnhub().getBaseUrl();
            String key = apiProperties.getFinnhub().getKey();
            if (key == null || key.isBlank()) {
                return fallbackQuote(symbol);
            }

            Map<String, Object> quote = webClient.get()
                    .uri(base + "/quote?symbol={s}&token={k}", symbol, key)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, r -> Mono.error(new RuntimeException("Finnhub quote failed")))
                    .bodyToMono(Map.class)
                    .block();

            long to = Instant.now().getEpochSecond();
            long from = LocalDate.now().minusDays(7).atStartOfDay().toEpochSecond(ZoneOffset.UTC);

            Map<String, Object> candle = webClient.get()
                    .uri(base + "/stock/candle?symbol={s}&resolution=D&from={f}&to={t}&token={k}", symbol, from, to, key)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            List<BigDecimal> closePrices = new ArrayList<>();
            Object closeRaw = candle != null ? candle.get("c") : null;
            if (closeRaw instanceof List<?> closes) {
                for (Object c : closes) {
                    closePrices.add(new BigDecimal(String.valueOf(c)));
                }
            }

            return StockQuoteDto.builder()
                    .symbol(symbol)
                    .currentPrice(new BigDecimal(String.valueOf(quote.getOrDefault("c", 0))))
                    .currentVolume(Long.parseLong(String.valueOf(quote.getOrDefault("v", 0))))
                    .closePrices(closePrices)
                    .build();
        } catch (Exception ex) {
            log.warn("Failed to fetch stock quote for {} from Finnhub. Trying Alpha Vantage. {}", symbol, ex.getMessage());
            return fetchFromAlphaVantageOrFallback(symbol);
        }
    }


    private StockQuoteDto fetchFromAlphaVantageOrFallback(String symbol) {
        try {
            String key = apiProperties.getAlphavantage().getKey();
            if (key == null || key.isBlank()) {
                return fallbackQuote(symbol);
            }
            Map<String, Object> response = webClient.get()
                    .uri(apiProperties.getAlphavantage().getBaseUrl() +
                            "?function=TIME_SERIES_DAILY&symbol={s}&apikey={k}", symbol, key)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            Object seriesObj = response != null ? response.get("Time Series (Daily)") : null;
            if (!(seriesObj instanceof Map<?, ?> series) || series.isEmpty()) {
                return fallbackQuote(symbol);
            }

            List<String> dates = series.keySet().stream().map(String::valueOf).sorted().toList();
            List<BigDecimal> closes = new ArrayList<>();
            long latestVolume = 0L;
            for (int i = Math.max(0, dates.size() - 5); i < dates.size(); i++) {
                Object dayObj = series.get(dates.get(i));
                if (dayObj instanceof Map<?, ?> dayMap) {
                    closes.add(new BigDecimal(String.valueOf(dayMap.get("4. close"))));
                    latestVolume = Long.parseLong(String.valueOf(dayMap.get("5. volume")));
                }
            }

            if (closes.isEmpty()) {
                return fallbackQuote(symbol);
            }

            return StockQuoteDto.builder()
                    .symbol(symbol)
                    .currentPrice(closes.get(closes.size() - 1))
                    .currentVolume(latestVolume)
                    .closePrices(closes)
                    .build();
        } catch (Exception ex) {
            log.warn("Alpha Vantage fallback failed for {}. Using synthetic data. {}", symbol, ex.getMessage());
            return fallbackQuote(symbol);
        }
    }

    public StockData saveSnapshot(StockQuoteDto quote) {
        Trend trend = computeTrend(quote.getClosePrices());
        StockData stock = stockDataRepository.findBySymbol(quote.getSymbol())
                .orElse(StockData.builder().symbol(quote.getSymbol()).build());
        stock.setPrice(quote.getCurrentPrice());
        stock.setTrend(trend);
        stock.setVolume(quote.getCurrentVolume());
        stock.setLastUpdated(Instant.now());
        return stockDataRepository.save(stock);
    }

    public Trend computeTrend(List<BigDecimal> closePrices) {
        if (closePrices == null || closePrices.size() < 2) {
            return Trend.FLAT;
        }
        BigDecimal first = closePrices.get(Math.max(0, closePrices.size() - 5));
        BigDecimal last = closePrices.get(closePrices.size() - 1);
        int cmp = last.compareTo(first);
        if (cmp > 0) return Trend.UP;
        if (cmp < 0) return Trend.DOWN;
        return Trend.FLAT;
    }

    private StockQuoteDto fallbackQuote(String symbol) {
        List<BigDecimal> synthetic = List.of(new BigDecimal("100"), new BigDecimal("101"), new BigDecimal("99"), new BigDecimal("103"), new BigDecimal("105"));
        return StockQuoteDto.builder()
                .symbol(symbol)
                .currentPrice(synthetic.get(synthetic.size() - 1))
                .currentVolume(1500000L)
                .closePrices(synthetic)
                .build();
    }
}
