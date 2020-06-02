package com.cvapp.coronatracker.service;
import com.cvapp.coronatracker.models.LocationStats;
import org.apache.commons.csv.CSVRecord;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    @Scheduled(cron = "0 0 0 * * *")
    public void fetchCoronaData() throws IOException, InterruptedException {
        //Creating the Client Connection Pool Manager by instantiating the PoolingHttpClientConnectionManager class.
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();

        //Set the maximum number of connections in the pool
        connManager.setMaxTotal(100);

        //Create a ClientBuilder Object by setting the connection manager
        HttpClientBuilder clientBuilder = HttpClients.custom().setConnectionManager(connManager);

        //Build the CloseableHttpClient object using the build() method.
        CloseableHttpClient httpclient = clientBuilder.build();

        //Creating the HttpGet requests
        HttpGet httpGetForConfirmed = new HttpGet(VIRUS_CONFIRMED_URL);
        HttpGet httpGetForRecovered = new HttpGet(VIRUS_RECOVERED_URL);
        HttpGet httpGetForDeaths = new HttpGet(VIRUS_DEATHS_URL);

        //Creating the Thread objects
        ServiceHelper threadForConfirmed = new ServiceHelper(httpclient, httpGetForConfirmed);
        ServiceHelper threadForRecovered = new ServiceHelper(httpclient, httpGetForRecovered);
        ServiceHelper threadForDeaths = new ServiceHelper(httpclient, httpGetForDeaths);

        //Starting all the threads
        threadForConfirmed.start();
        threadForRecovered.start();
        threadForDeaths.start();

        //Joining all the threads
        threadForConfirmed.join();
        threadForRecovered.join();
        threadForDeaths.join();

        //get all recovered cases
        Iterable<CSVRecord> recoveredRecords = threadForRecovered.getRecords();
        List<Integer> recoveredCases = new ArrayList<>();
        for (CSVRecord record : recoveredRecords) {
            recoveredCases.add(Integer.parseInt(record.get(record.size() - 1)));
        }
        Collections.reverse(recoveredCases);
        //iterator for list traversing in 'allStat' records
        int iteratorForRecovered = recoveredCases.size() - 1;


        //get all deaths
        Iterable<CSVRecord> deathsRecords = threadForDeaths.getRecords();
        List<Integer> deathCases = new ArrayList<>();
        for (CSVRecord record : deathsRecords) {
            deathCases.add(Integer.parseInt(record.get(record.size() - 1)));
        }
        Collections.reverse(deathCases);
        //iterator for list traversing in 'allStat' records
        int iteratorForDeaths = deathCases.size() - 1;


        List<LocationStats> newStats = new ArrayList<>();
        Iterable<CSVRecord> confirmedRecords = threadForConfirmed.getRecords();
        for (CSVRecord record : confirmedRecords) {
            LocationStats locationStats = new LocationStats();
            locationStats.setCountry(record.get("Country/Region"));
            locationStats.setState(record.get("Province/State"));
            int currentDayCases = Integer.parseInt(record.get(record.size() - 1));
            locationStats.setLatestTotalCases(currentDayCases);
            locationStats.setLatestRecoveredCases(recoveredCases.get(iteratorForRecovered));
            locationStats.setLatestDeathCases(deathCases.get(iteratorForDeaths));
            if (iteratorForRecovered > 0) {
                iteratorForRecovered--;
            }
            if (iteratorForDeaths > 0) {
                iteratorForDeaths--;
            }
            newStats.add(locationStats);
        }
        this.allStats = newStats;
    }
}
