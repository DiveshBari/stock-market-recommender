package com.example.stockdashboard.repository;

import com.example.stockdashboard.domain.StockData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StockDataRepository extends JpaRepository<StockData, Long> {
    Optional<StockData> findBySymbol(String symbol);
}
