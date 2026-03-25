package com.example.stockdashboard.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "news")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(length = 4000)
    private String description;

    @Column(nullable = false)
    private Integer sentimentScore;

    @Column(nullable = false)
    private String sector;

    @Column(nullable = false)
    private Instant publishedAt;

    @Column(nullable = false)
    private String relatedSymbol;
}
