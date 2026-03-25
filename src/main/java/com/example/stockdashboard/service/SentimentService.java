package com.example.stockdashboard.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SentimentService {

    private static final List<String> POSITIVE = List.of("growth", "surge", "rise", "profit", "expansion", "boom", "beat");
    private static final List<String> NEGATIVE = List.of("fall", "crisis", "shortage", "decline", "loss", "drop", "miss");

    public int score(String title, String description) {
        String text = (title + " " + description).toLowerCase();
        int score = 0;
        for (String p : POSITIVE) {
            if (text.contains(p)) score++;
        }
        for (String n : NEGATIVE) {
            if (text.contains(n)) score--;
        }
        return score;
    }
}
