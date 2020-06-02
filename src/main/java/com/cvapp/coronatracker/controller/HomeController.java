package com.cvapp.coronatracker.controller;

import com.cvapp.coronatracker.models.LocationStats;
import com.cvapp.coronatracker.service.CoronaVirusDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

@Controller
public class HomeController {

    @Autowired
    CoronaVirusDataService service;

    @GetMapping
    public String homePage(Model model) {
        List<LocationStats> allStats = service.getAllStats();
        int totalConfirmedCases = allStats.stream().mapToInt(stat -> stat.getLatestTotalCases()).sum();
        int totalRecoveredCases = allStats.stream().mapToInt(stat -> stat.getLatestRecoveredCases()).sum();
        int totalDeaths = allStats.stream().mapToInt(stat -> stat.getLatestDeathCases()).sum();

        model.addAttribute("totalConfirmedCases", NumberFormat.getNumberInstance(Locale.US).format(totalConfirmedCases));
        model.addAttribute("totalRecoveredCases", NumberFormat.getNumberInstance(Locale.US).format(totalRecoveredCases));
        model.addAttribute("totalDeaths", NumberFormat.getNumberInstance(Locale.US).format(totalDeaths));
        model.addAttribute("allStats", allStats);
        return "home";
    }
}
