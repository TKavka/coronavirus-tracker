package com.cvapp.coronatracker.controller;

import com.cvapp.coronatracker.models.LocationStats;
import com.cvapp.coronatracker.service.CoronaVirusDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    CoronaVirusDataService service;

    @GetMapping
    public String homePage(Model model) {
        List<LocationStats> allStats = service.getAllStats();
        int allCurrentDayCases = allStats.stream().mapToInt(stat -> stat.getLatestTotalCases()).sum();
        int totalNewCases = allStats.stream().mapToInt(stat -> stat.getDiffFromPreviousDay()).sum();
        model.addAttribute("allCurrentDayCases", allCurrentDayCases);
        model.addAttribute("totalNewCases", totalNewCases);
        model.addAttribute("allStats", allStats);
        model.addAttribute("message", "asdasf");
        return "home";
    }
}
