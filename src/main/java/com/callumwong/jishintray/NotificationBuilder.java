package com.callumwong.jishintray;

import java.net.URI;
import java.util.Map;

public class NotificationBuilder {
    private String title;
    private String description;
    private Map<String, String> fields;
    private URI image;

    public NotificationBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    public NotificationBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public NotificationBuilder setFields(Map<String, String> fields) {
        this.fields = fields;
        return this;
    }

    public NotificationBuilder setImage(URI image) {
        this.image = image;
        return this;
    }

    public Notification createNotification() {
        return new Notification(title, description, fields, image);
    }
}