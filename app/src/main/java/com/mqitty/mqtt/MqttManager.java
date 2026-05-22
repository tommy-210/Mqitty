package com.mqitty.mqtt;

import android.content.Context;
import com.mqitty.database.DataBaseHelper;
import com.mqitty.model.MessageModel;
import java.util.HashMap;
import java.util.Map;

public class MqttManager implements Mqtt.MqttMessageListener {
    private static MqttManager instance;
    private final Map<String, Mqtt> clients = new HashMap<>();
    private final Map<String, Integer> activeSubscriptions = new HashMap<>(); // broker|topic -> receiverId
    private DataBaseHelper dataBaseHelper;

    private MqttManager() {}

    public static synchronized MqttManager getInstance() {
        if (instance == null) {
            instance = new MqttManager();
        }
        return instance;
    }

    private String getSubscriptionKey(String broker, String topic) {
        return broker + "|" + topic;
    }

    public Mqtt getMqtt(Context context, String broker) {
        if (dataBaseHelper == null) {
            dataBaseHelper = DataBaseHelper.getInstance(context.getApplicationContext());
        }
        if (!clients.containsKey(broker)) {
            Mqtt mqtt = new Mqtt(context.getApplicationContext(), broker);
            mqtt.addMessageListener(this);
            clients.put(broker, mqtt);
        }
        return clients.get(broker);
    }

    public void addPersistentSubscription(String broker, String topic, int receiverId) {
        activeSubscriptions.put(getSubscriptionKey(broker, topic), receiverId);
    }

    public void removePersistentSubscription(String broker, String topic) {
        activeSubscriptions.remove(getSubscriptionKey(broker, topic));
    }

    public boolean isSubscribed(String broker, String topic) {
        return activeSubscriptions.containsKey(getSubscriptionKey(broker, topic));
    }

    @Override
    public void onMessageArrived(String topic, String message) {
        // We don't easily know the broker here from the topic alone.
        // But since we are listener for all clients, we can try to match.
        // Actually, Mqtt.java doesn't pass the broker to the listener.
        
        // Alternative: iterate through all active subscriptions and match topic.
        // If topics are unique across brokers, it's fine.
        // If not, we might need Mqtt to pass its broker name to the listener.
        
        for (Map.Entry<String, Integer> entry : activeSubscriptions.entrySet()) {
            String key = entry.getKey();
            if (key.endsWith("|" + topic)) {
                Integer receiverId = entry.getValue();
                MessageModel newMessage = new MessageModel(-1, message, false, System.currentTimeMillis());
                dataBaseHelper.addOneMessage(receiverId, newMessage);
            }
        }
    }
}
