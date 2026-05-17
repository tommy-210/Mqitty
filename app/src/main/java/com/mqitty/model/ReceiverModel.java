package com.mqitty.model;

public class ReceiverModel {

    private int id;
    private String name, description, broker, topic;

    public ReceiverModel(int id, String name, String description, String broker, String topic) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.broker = broker;
        this.topic = topic;
    }

    public ReceiverModel() {}

    @Override
    public String toString() {
        return "SendModel{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", broker='" + broker + '\'' +
                ", topic='" + topic + '\'' +
                '}';
    }

    //    getters and setters
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
}
