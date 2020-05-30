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
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
public class CoronaVirusDataService {
    private static String VIRUS_CONFIRMED_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv";
    private static String VIRUS_RECOVERED_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_recovered_global.csv";
    private static String VIRUS_DEATHS_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_deaths_global.csv";

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
//---------------------------------------------------------
        HttpClient httpClient2 = HttpClient.newHttpClient();
        HttpRequest request2 = HttpRequest.newBuilder()
                .uri(URI.create(VIRUS_RECOVERED_URL))
                .build();
        HttpResponse<String> response2 = httpClient2.send(request2, HttpResponse.BodyHandlers.ofString());

        StringReader reader2 = new StringReader(response2.body());
        Iterable<CSVRecord> records2 = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader2);
        List<Integer> recoveredCases = new ArrayList<>();
        for (CSVRecord record : records2) {
            recoveredCases.add(Integer.parseInt(record.get(record.size() - 1)));
        }

        Collections.reverse(recoveredCases);
        int i = recoveredCases.size() - 1;

//        for (int a : recoveredCases) {
//            System.out.println(a);
//        }

//-------------------------------------------------------
        HttpClient httpClient3 = HttpClient.newHttpClient();
        HttpRequest request3 = HttpRequest.newBuilder()
                .uri(URI.create(VIRUS_DEATHS_URL))
                .build();
        HttpResponse<String> response3 = httpClient3.send(request3, HttpResponse.BodyHandlers.ofString());

        StringReader reader3 = new StringReader(response3.body());
        Iterable<CSVRecord> records3 = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader3);
        List<Integer> deathCases = new ArrayList<>();
        for (CSVRecord record : records3) {
            deathCases.add(Integer.parseInt(record.get(record.size() - 1)));
        }

        Collections.reverse(deathCases);
        int j = deathCases.size() - 1;

        for (int a : deathCases) {
            System.out.println(a);
        }
//-------------------------------------------------------
        for (CSVRecord record : records) {
            LocationStats locationStats = new LocationStats();
            locationStats.setCountry(record.get("Country/Region"));
            locationStats.setState(record.get("Province/State"));
            int currentDayCases = Integer.parseInt(record.get(record.size() - 1));
            int previousDayCases = Integer.parseInt(record.get(record.size() - 2));
            locationStats.setLatestTotalCases(currentDayCases);
            locationStats.setLatestRecoveredCases(recoveredCases.get(i));
            locationStats.setLatestDeathCases(deathCases.get(j));
            if (i > 0) {
                i--;
            }

            if (j > 0) {
                j--;
            }
            newStats.add(locationStats);
            // System.out.println(locationStats);
        }
        this.allStats = newStats;
    }
}
