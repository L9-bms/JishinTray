package com.callumwong.jishintray;

import com.callumwong.jishintray.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.stream.Collectors;

public class P2PQuakeClient extends WebSocketClient {
    private static final Logger logger = LoggerFactory.getLogger(P2PQuakeClient.class);

    private final ObjectMapper mapper;

    public P2PQuakeClient(URI serverUri) {
        super(serverUri);

        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        logger.info("connected to p2pquake ws");
    }

    @Override
    public void onMessage(String message) {
        try {
            JsonNode node = mapper.readTree(message);
            if (!node.has("code")) return;
            int code = node.get("code").asInt();
            switch (code) {
                case 551: // Earthquake information
                    JMAQuake jmaQuake = mapper.readValue(message, JMAQuake.class);
                    logger.info(jmaQuake.toString());
                    break;
                case 552: // Tsunami forecast
                    JMATsunami jmaTsunami = mapper.readValue(message, JMATsunami.class);
                    logger.info(jmaTsunami.toString());
                    break;
                case 554: // EEW Detection
                    EEWDetection eewDetection = mapper.readValue(message, EEWDetection.class);
                    logger.info("EEW DETECTEDEWOOOO");
                    break;
                case 556: // EEW Alert
                    EEW eew = mapper.readValue(message, EEW.class);
                    if (eew.getAreas() != null) {
                        logger.info("Earthquake Early Warning in: {}", eew.getAreas().stream().map(EEWAllOfAreas::getPref).collect(Collectors.joining(",")));
                    }
                    break;
                case 555: // Number of peers
                case 561: // P2P Earthquake detection
                case 9611: // Analysis result
                    break;
                default:
                    logger.error("unexpected code: {}", code);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        logger.info("disconnected");
    }

    @Override
    public void onError(Exception e) {
        logger.error("error occured: {}", e.getMessage());
    }
}
