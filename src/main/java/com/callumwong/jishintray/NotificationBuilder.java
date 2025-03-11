package com.callumwong.jishintray;

import javax.swing.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("UnusedReturnValue")
public class NotificationBuilder {
    private String title;
    private String description;
    private Map<String, JComponent> fields;
    private URL image;

    public NotificationBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    public NotificationBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public NotificationBuilder setFields(Map<String, ?> fields) {
        Map<String, JComponent> map = new HashMap<>();
        fields.forEach((key, value) -> {
            if (value instanceof String) {
                map.put(key, new JLabel("<html>" + value + "</html>"));
            } else if (value instanceof JComponent) {
                map.put(key, (JComponent) value);
            } else {
                throw new IllegalArgumentException("Unsupported field type: " + value.getClass());
            }
        });
        this.fields = map;
        return this;
    }

    public NotificationBuilder setImage(URL image) {
        this.image = image;
        return this;
    }

    public Notification createNotification() {
        return new Notification(title, description, fields, image);
    }
}