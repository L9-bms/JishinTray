package com.callumwong.jishintray;

import com.callumwong.jishintray.model.JMAQuake;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;

public class History {
    private static final Logger logger = LoggerFactory.getLogger(History.class);

    private final ObjectMapper mapper;

    public History() {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public void getHistory() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.p2pquake.net/v2/history?codes=551")).build();
        List<JMAQuake> jmaQuake = Arrays.asList(mapper.readValue(client.send(request, HttpResponse.BodyHandlers.ofString()).body(), JMAQuake[].class));
        logger.info(jmaQuake.toString());
    }
}
