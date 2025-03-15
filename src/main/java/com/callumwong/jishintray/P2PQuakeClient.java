package com.callumwong.jishintray;

import com.callumwong.jishintray.frame.NotificationFrame;
import com.callumwong.jishintray.model.*;
import com.callumwong.jishintray.util.EnumUtil;
import com.callumwong.jishintray.util.TableColumnAdjuster;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dorkbox.systemTray.SystemTray;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class P2PQuakeClient extends WebSocketClient {
    private static final Logger log = LoggerFactory.getLogger(P2PQuakeClient.class);

    private final ObjectMapper mapper;
    private final SystemTray tray;

    public P2PQuakeClient(URI serverUri) {
        super(serverUri);

        tray = SystemTray.get();
        updateTrayStatus("Connecting to WebSocket...");

        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        log.info("connected to p2pquake ws");
        updateTrayStatus("Connected to P2PQuake");
    }

    @Override
    public void onMessage(String message) {
        try {
            JsonNode node = mapper.readTree(message);

            if (!node.has("code")) return;
            int code = node.get("code").asInt();

            log.debug(node.toString());

            String id;
            if (node.hasNonNull("id")) {
                id = node.get("id").asText();
            } else if (node.hasNonNull("_id")) {
                id = node.get("_id").asText();
            } else {
                log.error("json object does not have id or _id field: {}", node.toPrettyString());
                return;
            }

            NotificationFrame.Builder builder = new NotificationFrame.Builder();
            String imageUrl = String.format("https://cdn.p2pquake.net/app/images/%s_trim_big.png", id);
            try {
                builder.setImage(URI.create(imageUrl).toURL());
            } catch (MalformedURLException e) {
                log.error("error setting image", e);
            }

            switch (code) {
                case 551: // Earthquake information
                    JMAQuake jmaQuake = mapper.readValue(message, JMAQuake.class);

                    String earthquakeDescription = String.format("Issued on %s", jmaQuake.getIssue().getTime());
                    Map<String, String> earthquakeFields = new HashMap<>();

                    if (jmaQuake.getEarthquake().getMaxScale() != null) {
                        earthquakeFields.put("Maximum Intensity", EnumUtil.scaleToString(jmaQuake.getEarthquake().getMaxScale().getValue()));
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
                                String.format("Intensity %s", EnumUtil.scaleToString(scale.getValue())),
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
                    Map<String, JScrollPane> tsunamiFields = new HashMap<>();

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
                        List<String[]> tableRows = area.stream().map(a -> {
                            String firstHeight = "N/A";
                            if (a.getFirstHeight() != null) {
                                if (a.getFirstHeight().getCondition() != null) {
                                    firstHeight = EnumUtil.conditionToString(a.getFirstHeight().getCondition());
                                } else if (a.getFirstHeight().getArrivalTime() != null) {
                                    firstHeight = String.format("Arriving at %s", a.getFirstHeight().getArrivalTime());
                                }
                            }

                            String maxHeight = a.getMaxHeight() != null && a.getMaxHeight().getDescription() != null
                                    ? EnumUtil.maxHeightToString(a.getMaxHeight().getDescription())
                                    : "N/A";

                            return new String[]{a.getName(), firstHeight, maxHeight};
                        }).toList();

                        String[] columnNames = new String[]{"Area", "First Height", "Max Height"};

                        TableModel tableModel = new AbstractTableModel() {
                            @Override
                            public int getRowCount() {
                                return tableRows.size();
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
                                return tableRows.get(rowIndex)[columnIndex];
                            }
                        };

                        JTable table = new JTable(tableModel);

                        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                        TableColumnAdjuster tca = new TableColumnAdjuster(table);
                        tca.adjustColumns();

                        int cols = table.getColumnModel().getTotalColumnWidth();
                        int rows = table.getRowHeight() * table.getRowCount();
                        Dimension d = new Dimension(cols, rows);
                        table.setPreferredScrollableViewportSize(d);

                        JScrollPane jScrollPane = new JScrollPane();
                        jScrollPane.getViewport().add(table);

                        tsunamiFields.put(EnumUtil.gradeToString(grade), jScrollPane);
                    });

                    builder.setDescription(tsunamiDescription).setFields(tsunamiFields);

                    SwingUtilities.invokeLater(builder::createNotification);

                    break;
                case 554: // EEW detection
                    EEWDetection eewDetection = mapper.readValue(message, EEWDetection.class);

                    SwingUtilities.invokeLater(() -> new NotificationFrame.Builder()
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

                        Map<String, List<String>> groupedAreas = new HashMap<>();
                        if (eew.getAreas() != null) {
                            eew.getAreas().forEach(area -> {
                                if (!groupedAreas.containsKey(area.getPref())) {
                                    groupedAreas.put(area.getPref(), new ArrayList<>());
                                }
                                groupedAreas.get(area.getPref()).add(area.getName());
                            });
                        }

                        Map<String, String> eewFields = new HashMap<>();
                        groupedAreas.forEach((pref, areas) -> eewFields.put(pref, String.join("<br />", areas)));

                        builder.setDescription(eewDescription).setFields(eewFields);

                        SwingUtilities.invokeLater(builder::createNotification);
                    }

                    break;
                case 555: // Peers in area
                case 561: // P2P Userquake
                case 9611: // P2P Userquake Evaluation
                    break;
                default:
                    log.error("unexpected code: {}", code);
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
        log.info("kicked out from socket, reconnecting...");
        updateTrayStatus("Reconnecting to WebSocket...");

        Thread reconnectThread = new Thread(this::reconnect);
        reconnectThread.start();
    }

    @Override
    public void onError(Exception e) {
        log.error("error occured: {}", e.getMessage());
    }

    private void updateTrayStatus(String message) {
        if (tray == null) return;
        tray.setStatus(message);
    }
}
