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
    private final java.util.List<String> sentMessagesQueue = new java.util.ArrayList<>();
    private DataBaseHelper dataBaseHelper;

    public interface SubscriptionListener {
        void onSubscriptionsChanged(int count);
    }
    public interface MessageListener {
        void onMessageArrived(String topic, String message, boolean wasSentByUs);
    }
    private final java.util.List<SubscriptionListener> subscriptionListeners = new java.util.ArrayList<>();
    private final java.util.List<MessageListener> messageListeners = new java.util.ArrayList<>();

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

    public void addPersistentSubscription(Context context, String broker, String topic, int receiverId) {
        activeSubscriptions.put(getSubscriptionKey(broker, topic), receiverId);
        notifyListener();
        startService(context);
    }

    public void removePersistentSubscription(String broker, String topic) {
        activeSubscriptions.remove(getSubscriptionKey(broker, topic));
        notifyListener();
    }

    public void stopAllSubscriptions() {
        for (String key : activeSubscriptions.keySet()) {
            String[] parts = key.split("\\|");
            if (parts.length == 2) {
                Mqtt mqtt = clients.get(parts[0]);
                if (mqtt != null) {
                    mqtt.unsubscribe(parts[1]);
                }
            }
        }
        activeSubscriptions.clear();
        notifyListener();
    }

    public java.util.List<String> getActiveReceiverNames() {
        java.util.List<String> names = new java.util.ArrayList<>();
        for (Integer id : activeSubscriptions.values()) {
            com.mqitty.model.ReceiverModel model = dataBaseHelper.getReceiverById(id);
            if (model != null) {
                names.add(model.getName());
            }
        }
        return names;
    }

    public boolean isSubscribed(String broker, String topic) {
        return activeSubscriptions.containsKey(getSubscriptionKey(broker, topic));
    }

    public int getActiveSubscriptionCount() {
        return activeSubscriptions.size();
    }

    public void addToSentQueue(String message) {
        synchronized (sentMessagesQueue) {
            sentMessagesQueue.add(message);
        }
    }

    public boolean checkAndRemoveFromSentQueue(String message) {
        synchronized (sentMessagesQueue) {
            if (sentMessagesQueue.contains(message)) {
                sentMessagesQueue.remove(message);
                return true;
            }
        }
        return false;
    }

    public void addSubscriptionListener(SubscriptionListener listener) {
        if (!subscriptionListeners.contains(listener)) {
            subscriptionListeners.add(listener);
        }
    }

    public void removeSubscriptionListener(SubscriptionListener listener) {
        subscriptionListeners.remove(listener);
    }

    public void addMessageListener(MessageListener listener) {
        if (!messageListeners.contains(listener)) {
            messageListeners.add(listener);
        }
    }

    public void removeMessageListener(MessageListener listener) {
        messageListeners.remove(listener);
    }

    private void notifyListener() {
        for (SubscriptionListener listener : new java.util.ArrayList<>(subscriptionListeners)) {
            listener.onSubscriptionsChanged(activeSubscriptions.size());
        }
    }

    private void startService(Context context) {
        android.content.Intent serviceIntent = new android.content.Intent(context, MqttService.class);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }

    @Override
    public void onMessageArrived(String topic, String message) {
        // We don't easily know the broker here from the topic alone.
        // But since we are listener for all clients, we can try to match.
        // Actually, Mqtt.java doesn't pass the broker to the listener.
        
        // Alternative: iterate through all active subscriptions and match topic.
        // If topics are unique across brokers, it's fine.
        // If not, we might need Mqtt to pass its broker name to the listener.

        boolean wasSentByUs = false;
        synchronized (sentMessagesQueue) {
            if (sentMessagesQueue.contains(message)) {
                sentMessagesQueue.remove(message);
                wasSentByUs = true;
            }
        }
        
        for (Map.Entry<String, Integer> entry : activeSubscriptions.entrySet()) {
            String key = entry.getKey();
            if (key.endsWith("|" + topic)) {
                Integer receiverId = entry.getValue();
                MessageModel newMessage = new MessageModel(-1, message, wasSentByUs, System.currentTimeMillis());
                dataBaseHelper.addOneMessage(receiverId, newMessage);
            }
        }

        for (MessageListener listener : new java.util.ArrayList<>(messageListeners)) {
            listener.onMessageArrived(topic, message, wasSentByUs);
        }
    }
}
