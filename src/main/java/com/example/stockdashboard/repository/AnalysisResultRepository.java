package com.example.stockdashboard.repository;

import com.example.stockdashboard.domain.AnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, Long> {
    Optional<AnalysisResult> findTopByStockSymbolOrderByTimestampDesc(String stockSymbol);
    List<AnalysisResult> findTop100ByOrderByTimestampDesc();
}
