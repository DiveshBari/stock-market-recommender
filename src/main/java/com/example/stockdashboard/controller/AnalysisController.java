package com.example.stockdashboard.controller;

import com.example.stockdashboard.domain.AnalysisResult;
import com.example.stockdashboard.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;

    @PostMapping("/analyze")
    public String runAnalysis() {
        analysisService.runFullAnalysis();
        return "redirect:/dashboard";
    }
}

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
class AnalysisRestController {

    private final AnalysisService analysisService;

    @PostMapping("/analyze")
    public ResponseEntity<List<AnalysisResult>> analyze() {
        return ResponseEntity.ok(analysisService.runFullAnalysis());
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard() {
        return ResponseEntity.ok(analysisService.dashboardRows());
    }
}
