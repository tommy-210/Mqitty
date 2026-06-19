package com.mqitty.model;

import java.io.Serializable;

public class ReceiverModel implements Serializable {

    private int id, notificationType;
    private String name, description, broker, topic, keywordCustomNotification;

    public ReceiverModel(int id, String name, String description, String broker, String topic, int notificationType, String keywordCustomNotification) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.broker = broker;
        this.topic = topic;
        this.notificationType = notificationType;
        this.keywordCustomNotification = keywordCustomNotification;
    }

    @Override
    public String toString() {
        return "SendModel{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", broker='" + broker + '\'' +
                ", topic='" + topic + '\'' +
                ", notificationType='" + notificationType + '\'' +
                ", keywordCustomNotification='" + keywordCustomNotification + '\'' +
                '}';
    }

    //    getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBroker() {
        return broker;
    }

    public void setBroker(String broker) {
        this.broker = broker;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(int notificationType) {
        this.notificationType = notificationType;
    }

    public String getKeywordCustomNotification() {
        return keywordCustomNotification;
    }

    public void setKeywordCustomNotification(String keywordCustomNotification) {
        this.keywordCustomNotification = keywordCustomNotification;
    }
}
