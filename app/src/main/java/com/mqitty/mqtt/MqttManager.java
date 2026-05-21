package com.mqitty.mqtt;

import android.content.Context;
import java.util.HashMap;
import java.util.Map;

public class MqttManager {
    private static MqttManager instance;
    private final Map<String, Mqtt> clients = new HashMap<>();

    private MqttManager() {}

    public static synchronized MqttManager getInstance() {
        if (instance == null) {
            instance = new MqttManager();
        }
        return instance;
    }

    public Mqtt getMqtt(Context context, String broker) {
        if (!clients.containsKey(broker)) {
            clients.put(broker, new Mqtt(context.getApplicationContext(), broker));
        }
        return clients.get(broker);
    }
}
