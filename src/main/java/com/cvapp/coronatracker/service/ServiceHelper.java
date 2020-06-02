package com.cvapp.coronatracker.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import java.io.IOException;
import java.io.StringReader;

public class ServiceHelper extends Thread {
    private CloseableHttpClient httpClient;
    private HttpGet httpGet;
    private Iterable<CSVRecord> records;

    ServiceHelper(CloseableHttpClient httpClient, HttpGet httpGet) {
        this.httpClient = httpClient;
        this.httpGet = httpGet;
    }

    @Override
    public void run() {
        try {
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity httpEntity = response.getEntity();
            StringReader reader = new StringReader(EntityUtils.toString(httpEntity));
            records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    Iterable<CSVRecord> getRecords() {
        return records;
    }
}
