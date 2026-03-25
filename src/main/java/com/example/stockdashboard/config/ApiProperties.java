package com.example.stockdashboard.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "api")
public class ApiProperties {
    private Finnhub finnhub = new Finnhub();
    private AlphaVantage alphavantage = new AlphaVantage();

    @Data
    public static class Finnhub {
        private String baseUrl;
        private String key;
    }

    @Data
    public static class AlphaVantage {
        private String baseUrl;
        private String key;
    }
}
