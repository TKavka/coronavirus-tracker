package com.cvapp.coronatracker.service;

import com.cvapp.coronatracker.models.LocationStats;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
public class CoronaVirusDataService {
    private static String VIRUS_CONFIRMED_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv";

    private List<LocationStats> allStats = new ArrayList<>();

    public List<LocationStats> getAllStats() {
        return allStats;
    }

    @PostConstruct
    @Scheduled(cron = "* * * 1 * *")
    public void fetchCoronaData() throws IOException, InterruptedException {
        List<LocationStats> newStats = new ArrayList<>();

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(VIRUS_CONFIRMED_URL))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        StringReader reader = new StringReader(response.body());
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader);

        for (CSVRecord record : records) {
            LocationStats locationStats = new LocationStats();
            locationStats.setCountry(record.get("Country/Region"));
            locationStats.setState(record.get("Province/State"));
            int currentDayCases = Integer.parseInt(record.get(record.size() - 1));
            int previousDayCases = Integer.parseInt(record.get(record.size() - 2));
            locationStats.setLatestTotalCases(currentDayCases);
            locationStats.setDiffFromPreviousDay(currentDayCases - previousDayCases);
            newStats.add(locationStats);
           // System.out.println(locationStats);
        }
        this.allStats = newStats;
    }
}
