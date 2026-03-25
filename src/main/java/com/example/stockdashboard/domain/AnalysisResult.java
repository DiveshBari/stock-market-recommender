package com.example.stockdashboard.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "analysis")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String stockSymbol;

    @Column(nullable = false)
    private Integer sentimentScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Trend trend;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Prediction prediction;

    @Column(nullable = false)
    private Instant timestamp;
}
