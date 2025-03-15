package com.callumwong.jishintray;

import com.callumwong.jishintray.frame.NotificationFrame;
import com.callumwong.jishintray.model.*;
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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.callumwong.jishintray.util.StringUtil.*;

public class P2PQuakeClient extends WebSocketClient {
    private static final Logger log = LoggerFactory.getLogger(P2PQuakeClient.class);

    private final ObjectMapper mapper;
    private final SystemTray tray;

    public P2PQuakeClient(URI serverUri) {
        super(serverUri);

        tray = SystemTray.get();
        updateTrayStatus(getLocalizedString("tray.status.connecting"));

        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        updateTrayStatus(getLocalizedString("tray.status.connected"));
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
                log.error(getLocalizedString("error.websocket.no_id"));
                return;
            }

            NotificationFrame.Builder builder = new NotificationFrame.Builder();
            String imageUrl = String.format("https://cdn.p2pquake.net/app/images/%s_trim_big.png", id);
            try {
                builder.setImage(URI.create(imageUrl).toURL());
            } catch (MalformedURLException e) {
                log.error(getLocalizedString("error.image.url"), e);
            }

            switch (code) {
                case 551: // Earthquake information
                    JMAQuake jmaQuake = mapper.readValue(message, JMAQuake.class);

                    String earthquakeDescription = MessageFormat.format(
                            getLocalizedString("string.earthquake.issued"), issueTimeToLocalizedString(jmaQuake.getIssue().getTime()));
                    Map<String, String> earthquakeFields = new HashMap<>();

                    if (jmaQuake.getEarthquake().getMaxScale() != null) {
                        earthquakeFields.put(getLocalizedString("string.earthquake.max_intensity"),
                                scaleToString(jmaQuake.getEarthquake().getMaxScale().getValue()));
                    }

                    if (jmaQuake.getIssue().getType() == JMAQuakeAllOfIssue.TypeEnum.SCALE_PROMPT) {
                        builder.setTitle(getLocalizedString("string.earthquake.scale_prompt.title"));
                        earthquakeDescription += "<br />" + getLocalizedString("string.earthquake.scale_prompt.description");

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
                                MessageFormat.format(getLocalizedString("string.earthquake.intensity"), scaleToString(scale.getValue())),
                                String.join("<br />", prefs))
                        );
                    } else {
                        builder.setTitle(switch (jmaQuake.getIssue().getType()) {
                            case DESTINATION:
                                yield getLocalizedString("string.earthquake.destination.title");
                            case FOREIGN:
                                yield getLocalizedString("string.earthquake.foreign.title");
                            case OTHER:
                                yield getLocalizedString("string.earthquake.other.title");
                            case SCALE_AND_DESTINATION:
                            case DETAIL_SCALE:
                                yield getLocalizedString("string.earthquake.title");
                            default:
                                log.error(getLocalizedString("error.websocket.type"), jmaQuake.getIssue().getType());
                                yield getLocalizedString("string.earthquake.title");
                        });

                        addHypocenterInfo(jmaQuake, earthquakeFields);

                        if (jmaQuake.getEarthquake().getDomesticTsunami() != null)
                            earthquakeFields.put(getLocalizedString("string.earthquake.tsunami"),
                                    domesticTsunamiToString(jmaQuake.getEarthquake().getDomesticTsunami()));
                        if (jmaQuake.getEarthquake().getForeignTsunami() != null)
                            earthquakeFields.put(getLocalizedString("string.earthquake.tsunami.foreign"),
                                    foreignTsunamiToString(jmaQuake.getEarthquake().getForeignTsunami()));
                    }

                    builder.setDescription(earthquakeDescription).setFields(earthquakeFields);

                    SwingUtilities.invokeLater(builder::createNotification);

                    break;
                case 552: // Tsunami information
                    JMATsunami jmaTsunami = mapper.readValue(message, JMATsunami.class);

                    builder.setTitle(getLocalizedString("string.tsunami.title"));

                    String tsunamiDescription = MessageFormat.format(getLocalizedString("string.tsunami.description"),
                            jmaTsunami.getIssue().getTime());
                    Map<String, JScrollPane> tsunamiFields = new HashMap<>();

                    Map<JMATsunamiAllOfAreas.GradeEnum, List<JMATsunamiAllOfAreas>> groupedTsunami = new HashMap<>();
                    if (jmaTsunami.getCancelled()) {
                        tsunamiDescription += "<br /><br />" + getLocalizedString("string.cancelled");
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
                                    firstHeight = conditionToString(a.getFirstHeight().getCondition());
                                } else if (a.getFirstHeight().getArrivalTime() != null) {
                                    firstHeight = String.format(getLocalizedString("string.tsunami.first_height"),
                                            a.getFirstHeight().getArrivalTime());
                                }
                            }

                            String maxHeight = a.getMaxHeight() != null && a.getMaxHeight().getDescription() != null
                                    ? maxHeightToString(a.getMaxHeight().getDescription())
                                    : "N/A";

                            return new String[]{a.getName(), firstHeight, maxHeight};
                        }).toList();

                        String[] columnNames = new String[]{
                                getLocalizedString("string.tsunami.column.area"),
                                getLocalizedString("string.tsunami.column.first_height"),
                                getLocalizedString("string.tsunami.column.max_height")
                        };

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

                        tsunamiFields.put(gradeToString(grade), jScrollPane);
                    });

                    builder.setDescription(tsunamiDescription).setFields(tsunamiFields);

                    SwingUtilities.invokeLater(builder::createNotification);

                    break;
                case 554: // EEW detection
                    EEWDetection eewDetection = mapper.readValue(message, EEWDetection.class);

                    SwingUtilities.invokeLater(() -> new NotificationFrame.Builder()
                            .setTitle(getLocalizedString("string.earthquake.eew.title"))
                            .setDescription(getLocalizedString("string.earthquake.eew.description.detection"))
                            .createNotification());

                    break;
                case 556: // EEW alert
                    EEW eew = mapper.readValue(message, EEW.class);

                    builder.setTitle(getLocalizedString("string.earthquake.eew.title"));
                    String eewDescription = getLocalizedString("string.earthquake.eew.description.alert");

                    if (Boolean.TRUE.equals(eew.getTest())) return;
                    if (eew.getAreas() != null) {
                        if (eew.getCancelled()) {
                            eewDescription = getLocalizedString("string.cancelled") + "<br /><br />" + eewDescription;
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
                    log.error(getLocalizedString("error.websocket.code"), code);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void addHypocenterInfo(JMAQuake jmaQuake, Map<String, String> fields) {
        if (jmaQuake.getEarthquake().getHypocenter() != null)
            fields.put(getLocalizedString("string.earthquake.hypocenter"), String.format("%s (%s, %s)",
                    jmaQuake.getEarthquake().getHypocenter().getName(),
                    jmaQuake.getEarthquake().getHypocenter().getLatitude(),
                    jmaQuake.getEarthquake().getHypocenter().getLongitude()
            ));

        if (jmaQuake.getEarthquake().getHypocenter().getMagnitude() != null)
            fields.put(getLocalizedString("string.earthquake.magnitude"),
                    jmaQuake.getEarthquake().getHypocenter().getMagnitude().toString());

        if (jmaQuake.getEarthquake().getHypocenter().getDepth() != null)
            fields.put(getLocalizedString("string.earthquake.depth"),
                    jmaQuake.getEarthquake().getHypocenter().getDepth().toString() + " km");
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        updateTrayStatus(getLocalizedString("tray.status.reconnecting"));

        Thread reconnectThread = new Thread(this::reconnect);
        reconnectThread.start();
    }

    @Override
    public void onError(Exception e) {
        log.error(getLocalizedString("error.websocket.error"), e.getMessage());
    }

    private void updateTrayStatus(String message) {
        if (tray == null) return;
        tray.setStatus(message);
    }
}
