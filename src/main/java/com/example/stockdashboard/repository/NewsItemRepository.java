package com.example.stockdashboard.repository;

import com.example.stockdashboard.domain.NewsItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface NewsItemRepository extends JpaRepository<NewsItem, Long> {
    List<NewsItem> findTop50ByOrderByPublishedAtDesc();
    List<NewsItem> findByRelatedSymbolAndPublishedAtAfter(String relatedSymbol, Instant after);
}
