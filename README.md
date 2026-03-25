# Stock Market Recommender Dashboard

Spring Boot full-stack application that pulls stock + financial news data, scores news sentiment, and predicts stock direction for a daily dashboard.

## Features
- `/dashboard` Thymeleaf UI for daily insights.
- `/analyze` trigger analysis manually.
- `/api/analyze` and `/api/dashboard` REST endpoints.
- Daily scheduled analysis job.
- Finnhub integration (with graceful fallback when API key is missing/rate-limited).
- Sentiment scoring, sector mapping, and rule-based prediction engine.

## Run
```bash
mvn spring-boot:run
```
Open: http://localhost:8080/dashboard

## Configuration
Set environment variables:
- `FINNHUB_API_KEY`
- `ALPHAVANTAGE_API_KEY` (optional)
- `DAILY_ANALYSIS_CRON` (optional)

Default profile uses H2. For PostgreSQL:
```bash
SPRING_PROFILES_ACTIVE=postgres mvn spring-boot:run
```

## Architecture
- Controller: `DashboardController`, `AnalysisController`
- Services: `StockService`, `NewsService`, `AnalysisService`, `SentimentService`, `SectorMappingService`
- Repository: JPA repositories for `stocks`, `news`, `analysis`
- Scheduler: `DailyAnalysisScheduler`
