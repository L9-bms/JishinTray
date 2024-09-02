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

import javax.swing.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

            String id;
            if (node.hasNonNull("id")) {
                id = node.get("id").asText();
            } else if (node.hasNonNull("_id")) {
                id = node.get("_id").asText();
            } else {
                logger.error("json object does not have id or _id field: {}", node.toPrettyString());
                return;
            }

            switch (code) {
                case 551: // Earthquake information
                    JMAQuake jmaQuake = mapper.readValue(message, JMAQuake.class);

                    String imageUrl = String.format("https://cdn.p2pquake.net/app/images/%s_trim_big.png", id);
//                    String imageUrl = "https://cdn.p2pquake.net/app/images/66d453c4d616be440743d431_trim_big.png";
                    logger.info(jmaQuake.toString());

                    NotificationBuilder builder = new NotificationBuilder();
                    String description = String.format("Issued on %s", jmaQuake.getIssue().getTime());
                    Map<String, String> fields = new HashMap<>();

                    try {
                        builder.setImage(URI.create(imageUrl).toURL());
                    } catch (MalformedURLException e) {
                        logger.error("error setting image", e);
                    }

                    switch (jmaQuake.getIssue().getType()) {
                        case SCALE_PROMPT -> {
                            builder.setTitle("Earthquake Seismic Intensity Information");
                            description += "<br />Epicenter and tsunami information is under investigation.";
                            fields.put("Maximum Intensity", jmaQuake.getEarthquake().getMaxScale().toString());

                            Map<JMAQuakeAllOfPoints.ScaleEnum, List<String>> groupedIntensities = new HashMap<>();
                            for (JMAQuakeAllOfPoints point : jmaQuake.getPoints()) {
                                JMAQuakeAllOfPoints.ScaleEnum scale = point.getScale();

                                if (!groupedIntensities.containsKey(scale)) {
                                    groupedIntensities.put(scale, new ArrayList<>());
                                }
                                groupedIntensities.get(scale).add(point.getAddr());
                            }

                            groupedIntensities.forEach((scaleEnum, prefs) ->
                                    fields.put(scaleEnum.getValue().toString(), String.join("<br />", prefs)));
                        }
                        case DESTINATION -> {
                            builder.setTitle("Earthquake Epicenter Information");
                            fields.put("Hypocenter", String.format("%s (%s, %s)",
                                    jmaQuake.getEarthquake().getHypocenter().getName(),
                                    jmaQuake.getEarthquake().getHypocenter().getLatitude(),
                                    jmaQuake.getEarthquake().getHypocenter().getLongitude()
                            ));
                            fields.put("Magnitude", jmaQuake.getEarthquake().getHypocenter().getMagnitude().toString());
                            fields.put("Depth", jmaQuake.getEarthquake().getHypocenter().getDepth().toString() + " km");
                        }
                        default -> {
                            builder.setTitle(switch (jmaQuake.getIssue().getType()) {
                                case SCALE_AND_DESTINATION:
                                case DETAIL_SCALE:
                                    yield "Earthquake Information";
                                case FOREIGN:
                                    yield "Foreign Earthquake Information";
                                case OTHER:
                                    yield "Other Earthquake Information";
                                default:
                                    throw new IllegalStateException("Unexpected value: " + jmaQuake.getIssue().getType());
                            });

                            fields.put("Hypocenter", String.format("%s (%s, %s)",
                                    jmaQuake.getEarthquake().getHypocenter().getName(),
                                    jmaQuake.getEarthquake().getHypocenter().getLatitude(),
                                    jmaQuake.getEarthquake().getHypocenter().getLongitude()
                            ));
                            fields.put("Magnitude", jmaQuake.getEarthquake().getHypocenter().getMagnitude().toString());
                            fields.put("Depth", jmaQuake.getEarthquake().getHypocenter().getDepth().toString() + " km");
                            fields.put("Maximum Intensity", jmaQuake.getEarthquake().getMaxScale().toString());
                            fields.put("Tsunami", jmaQuake.getEarthquake().getDomesticTsunami().getValue());
                            fields.put("Foreign Tsunami", jmaQuake.getEarthquake().getForeignTsunami().getValue());
                        }
                    }

                    builder.setDescription("<html>" + description + "</html>").setFields(fields);

                    SwingUtilities.invokeLater(builder::createNotification);

                    break;
                case 552: // Tsunami information
                    JMATsunami jmaTsunami = mapper.readValue(message, JMATsunami.class);
                    logger.info(jmaTsunami.toString());
                    break;
                case 554: // EEW detection
                    EEWDetection eewDetection = mapper.readValue(message, EEWDetection.class);

                    SwingUtilities.invokeLater(() -> new NotificationBuilder()
                            .setTitle("Earthquake Early Warning")
                            .setDescription("An earthquake early warning has been issued.")
                            .createNotification());

                    break;
                case 556: // EEW alert
                    EEW eew = mapper.readValue(message, EEW.class);
                    if (eew.getAreas() != null) {
                        logger.info("Earthquake Early Warning in: {}", eew.getAreas().stream().map(EEWAllOfAreas::getPref).collect(Collectors.joining(",")));
                    }
                    break;
                case 555: // Peers in area
                case 561: // P2P Userquake
                case 9611: // P2P Userquake Evaluation
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
        logger.info("kicked out from socket, reconnecting...");
        try {
            reconnectBlocking();
        } catch (InterruptedException e) {
            logger.error("error reconnecting to socket", e);
        }
    }

    @Override
    public void onError(Exception e) {
        logger.error("error occured: {}", e.getMessage());
    }
}
