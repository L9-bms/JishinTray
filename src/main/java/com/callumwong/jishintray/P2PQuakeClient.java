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
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

            logger.debug(node.toString());

            String id;
            if (node.hasNonNull("id")) {
                id = node.get("id").asText();
            } else if (node.hasNonNull("_id")) {
                id = node.get("_id").asText();
            } else {
                logger.error("json object does not have id or _id field: {}", node.toPrettyString());
                return;
            }

            NotificationBuilder builder = new NotificationBuilder();

            switch (code) {
                case 551: // Earthquake information
                    JMAQuake jmaQuake = mapper.readValue(message, JMAQuake.class);

                    String imageUrl = String.format("https://cdn.p2pquake.net/app/images/%s_trim_big.png", id);
                    String earthquakeDescription = String.format("Issued on %s", jmaQuake.getIssue().getTime());

                    Map<String, String> earthquakeFields = new HashMap<>();

                    try {
                        builder.setImage(URI.create(imageUrl).toURL());
                    } catch (MalformedURLException e) {
                        logger.error("error setting image", e);
                    }

                    if (jmaQuake.getEarthquake().getMaxScale() != null) {
                        earthquakeFields.put("Maximum Intensity", Util.scaleToString(jmaQuake.getEarthquake().getMaxScale().getValue()));
                    }

                    if (jmaQuake.getIssue().getType() == JMAQuakeAllOfIssue.TypeEnum.SCALE_PROMPT) {
                        builder.setTitle("Earthquake Seismic Intensity Information");
                        earthquakeDescription += "<br />Epicenter and tsunami information is under investigation.";

                        Map<JMAQuakeAllOfPoints.ScaleEnum, List<String>> groupedIntensities = new HashMap<>();
                        if (jmaQuake.getPoints() != null) {
                            for (JMAQuakeAllOfPoints point : jmaQuake.getPoints()) {
                                JMAQuakeAllOfPoints.ScaleEnum scale = point.getScale();

                                if (!groupedIntensities.containsKey(scale)) {
                                    groupedIntensities.put(scale, new ArrayList<>());
                                }
                                groupedIntensities.get(scale).add(point.getAddr());
                            }
                        }

                        groupedIntensities.forEach((scale, prefs) -> earthquakeFields.put(
                                String.format("Intensity %s", Util.scaleToString(scale.getValue())),
                                String.join("<br />", prefs))
                        );
                    } else {
                        builder.setTitle(switch (jmaQuake.getIssue().getType()) {
                            case DESTINATION:
                                yield "Earthquake Epicenter Information";
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

                        addHypocenterInfo(jmaQuake, earthquakeFields);

                        if (jmaQuake.getEarthquake().getDomesticTsunami() != null) {
                            earthquakeFields.put("Tsunami", jmaQuake.getEarthquake().getDomesticTsunami().getValue());
                        }
                        if (jmaQuake.getEarthquake().getForeignTsunami() != null) {
                            earthquakeFields.put("Foreign Tsunami", jmaQuake.getEarthquake().getForeignTsunami().getValue());
                        }
                    }

                    builder.setDescription(earthquakeDescription).setFields(earthquakeFields);

                    SwingUtilities.invokeLater(builder::createNotification);

                    break;
                case 552: // Tsunami information
                    JMATsunami jmaTsunami = mapper.readValue(message, JMATsunami.class);

                    builder.setTitle("Tsunami Information");

                    String tsunamiDescription = String.format("Issued on %s", jmaTsunami.getIssue().getTime());
                    Map<String, JTable> tsunamiFields = new HashMap<>();

                    Map<JMATsunamiAllOfAreas.GradeEnum, List<JMATsunamiAllOfAreas>> groupedTsunami = new HashMap<>();
                    if (jmaTsunami.getCancelled()) {
                        tsunamiDescription += "<br /><br />This tsunami warning has been cancelled.";
                    } else {
                        if (jmaTsunami.getAreas() != null) {
                            for (JMATsunamiAllOfAreas area : jmaTsunami.getAreas()) {
                                JMATsunamiAllOfAreas.GradeEnum grade = area.getGrade();

                                if (!groupedTsunami.containsKey(grade)) {
                                    groupedTsunami.put(grade, new ArrayList<>());
                                }
                                groupedTsunami.get(grade).add(area);
                            }
                        }
                    }

                    groupedTsunami.forEach((grade, area) -> {
                        List<String[]> rows = area.stream().map(a -> {
                            String firstHeight = "N/A";
                            if (a.getFirstHeight() != null) {
                                if (a.getFirstHeight().getCondition() != null) {
                                    firstHeight = Util.conditionToString(a.getFirstHeight().getCondition());
                                } else if (a.getFirstHeight().getArrivalTime() != null) {
                                    firstHeight = String.format("Arriving at %s", a.getFirstHeight().getArrivalTime());
                                }
                            }

                            String maxHeight = a.getMaxHeight() != null && a.getMaxHeight().getDescription() != null
                                    ? Util.maxHeightToString(a.getMaxHeight().getDescription())
                                    : "N/A";

                            return new String[]{a.getName(), firstHeight, maxHeight};
                        }).toList();

                        String[] columnNames = new String[]{"Area", "First Height", "Max Height"};

                        TableModel tableModel = new AbstractTableModel() {
                            @Override
                            public int getRowCount() {
                                return rows.size();
                            }

                            @Override
                            public int getColumnCount() {
                                return columnNames.length;
                            }

                            @Override
                            public String getColumnName(int column) {
                                return columnNames[column];
                            }

                            @Override
                            public Object getValueAt(int rowIndex, int columnIndex) {
                                return rows.get(rowIndex)[columnIndex];
                            }
                        };

                        tsunamiFields.put(Util.gradeToString(grade), new JTable(tableModel));
                    });

                    builder.setDescription(tsunamiDescription).setFields(tsunamiFields);

                    SwingUtilities.invokeLater(builder::createNotification);

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

                    builder.setTitle("Earthquake Early Warning");
                    String eewDescription = """
                                        An earthquake early warning has been issued.<br />
                                        In the following prefectures, please beware of strong tremors.
                                        """;

                    if (Boolean.TRUE.equals(eew.getTest())) return;
                    if (eew.getAreas() != null) {
                        if (eew.getCancelled()) {
                            eewDescription = "This warning has been cancelled.<br /><br />" + eewDescription;
                        }

                        Map<String, String> eewFields = new HashMap<>();
                        eew.getAreas().forEach(area -> eewFields.put(area.getPref(), area.getName()));

                        builder.setDescription(eewDescription).setFields(eewFields);

                        SwingUtilities.invokeLater(builder::createNotification);
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

    private void addHypocenterInfo(JMAQuake jmaQuake, Map<String, String> fields) {
        if (jmaQuake.getEarthquake().getHypocenter() != null) {
            fields.put("Hypocenter", String.format("%s (%s, %s)",
                    jmaQuake.getEarthquake().getHypocenter().getName(),
                    jmaQuake.getEarthquake().getHypocenter().getLatitude(),
                    jmaQuake.getEarthquake().getHypocenter().getLongitude()
            ));
        }
        if (jmaQuake.getEarthquake().getHypocenter().getMagnitude() != null) {
            fields.put("Magnitude", jmaQuake.getEarthquake().getHypocenter().getMagnitude().toString());
        }
        if (jmaQuake.getEarthquake().getHypocenter().getDepth() != null) {
            fields.put("Depth", jmaQuake.getEarthquake().getHypocenter().getDepth().toString() + " km");
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        logger.info("kicked out from socket, reconnecting...");
        Thread reconnectThread = new Thread(this::reconnect);

        reconnectThread.start();
    }

    @Override
    public void onError(Exception e) {
        logger.error("error occured: {}", e.getMessage());
    }
}
