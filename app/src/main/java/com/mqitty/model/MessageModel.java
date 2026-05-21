package com.mqitty.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MessageModel {

    private int id;
    private String message;
    private boolean isSend;

    private long timestamp;

    public MessageModel(int id, String message, boolean isSend, long timestamp) {
        this.id = id;
        this.message = message;
        this.isSend = isSend;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "MessageModel{" +
                "id=" + id +
                ", message='" + message + '\'' +
                ", isSend=" + isSend +
                ", timestamp=" + timestamp +
                '}';
    }

    public String getFormattedTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    //    getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSend() {
        return isSend;
    }

    public void setSend(boolean send) {
        isSend = send;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
