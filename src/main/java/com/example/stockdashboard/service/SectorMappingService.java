package com.example.stockdashboard.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SectorMappingService {

    private static final Map<String, String> KEYWORD_TO_SECTOR = Map.ofEntries(
            Map.entry("gas shortage", "Energy"),
            Map.entry("oil", "Energy"),
            Map.entry("ai", "Tech"),
            Map.entry("cloud", "Tech"),
            Map.entry("ev", "Electric"),
            Map.entry("electric vehicle", "Electric")
    );

    private static final Map<String, List<String>> SECTOR_TO_STOCKS = Map.of(
            "Energy", List.of("ONGC", "RELIANCE"),
            "Electric", List.of("TATAPOWER", "ADANIGREEN"),
            "Tech", List.of("TCS", "INFY")
    );

    public String inferSector(String text) {
        String normalized = text == null ? "" : text.toLowerCase();
        return KEYWORD_TO_SECTOR.entrySet().stream()
                .filter(e -> normalized.contains(e.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse("General");
    }

    public List<String> stocksForSector(String sector) {
        return SECTOR_TO_STOCKS.getOrDefault(sector, List.of());
    }
}
