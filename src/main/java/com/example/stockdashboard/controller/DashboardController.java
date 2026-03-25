package com.example.stockdashboard.controller;

import com.example.stockdashboard.service.AnalysisService;
import com.example.stockdashboard.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final AnalysisService analysisService;
    private final NewsService newsService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("rows", analysisService.dashboardRows());
        model.addAttribute("news", newsService.latestNews());
        return "dashboard";
    }
}
