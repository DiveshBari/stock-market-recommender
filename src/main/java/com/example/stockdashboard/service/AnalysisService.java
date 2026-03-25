package com.example.stockdashboard.service;

import com.example.stockdashboard.domain.AnalysisResult;
import com.example.stockdashboard.domain.NewsItem;
import com.example.stockdashboard.domain.Prediction;
import com.example.stockdashboard.domain.StockData;
import com.example.stockdashboard.domain.Trend;
import com.example.stockdashboard.dto.DashboardRowDto;
import com.example.stockdashboard.dto.NewsArticleDto;
import com.example.stockdashboard.dto.StockQuoteDto;
import com.example.stockdashboard.repository.AnalysisResultRepository;
import com.example.stockdashboard.repository.NewsItemRepository;
import com.example.stockdashboard.repository.StockDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final StockService stockService;
    private final NewsService newsService;
    private final StockDataRepository stockDataRepository;
    private final NewsItemRepository newsItemRepository;
    private final AnalysisResultRepository analysisResultRepository;

    private static final List<String> WATCHLIST = List.of("RELIANCE", "TCS", "INFY", "ONGC", "TATAPOWER");

    public List<AnalysisResult> runFullAnalysis() {
        List<AnalysisResult> results = new ArrayList<>();
        for (String symbol : WATCHLIST) {
            StockQuoteDto quote = stockService.fetchStockQuote(symbol);
            StockData stock = stockService.saveSnapshot(quote);

            List<NewsArticleDto> fetched = newsService.fetchNews(symbol);
            newsService.saveNews(fetched);

            int sentimentScore = aggregateSentiment(symbol);
            boolean volumeHigh = stock.getVolume() > 1_000_000;
            Prediction prediction = predict(sentimentScore, stock.getTrend(), volumeHigh);

            AnalysisResult result = AnalysisResult.builder()
                    .stockSymbol(symbol)
                    .sentimentScore(sentimentScore)
                    .trend(stock.getTrend())
                    .prediction(prediction)
                    .timestamp(Instant.now())
                    .build();
            results.add(analysisResultRepository.save(result));
        }
        return results;
    }

    public List<DashboardRowDto> dashboardRows() {
        return stockDataRepository.findAll().stream()
                .sorted(Comparator.comparing(StockData::getSymbol))
                .map(stock -> {
                    AnalysisResult latest = analysisResultRepository.findTopByStockSymbolOrderByTimestampDesc(stock.getSymbol())
                            .orElse(AnalysisResult.builder().stockSymbol(stock.getSymbol()).trend(stock.getTrend()).sentimentScore(0).prediction(Prediction.NEUTRAL).timestamp(Instant.now()).build());
                    return DashboardRowDto.builder()
                            .symbol(stock.getSymbol())
                            .price(stock.getPrice())
                            .trend(stock.getTrend())
                            .sentimentScore(latest.getSentimentScore())
                            .prediction(latest.getPrediction())
                            .build();
                })
                .toList();
    }

    public Prediction predict(int sentimentScore, Trend trend, boolean volumeHigh) {
        if (sentimentScore > 2 && trend == Trend.UP && volumeHigh) return Prediction.STRONG_BUY;
        if (sentimentScore > 0 && trend == Trend.UP) return Prediction.BUY;
        if (sentimentScore < -2 && trend == Trend.DOWN) return Prediction.STRONG_SELL;
        if (sentimentScore < 0 && trend == Trend.DOWN) return Prediction.SELL;
        return Prediction.NEUTRAL;
    }

    private int aggregateSentiment(String symbol) {
        Instant since = Instant.now().minus(2, ChronoUnit.DAYS);
        List<NewsItem> items = newsItemRepository.findByRelatedSymbolAndPublishedAtAfter(symbol, since);
        return items.stream().mapToInt(NewsItem::getSentimentScore).sum();
    }
}
