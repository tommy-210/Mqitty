package com.mqitty.mqtt;

import android.content.Context;
import android.util.Log;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import java.util.ArrayList;
import java.util.List;

public class Mqtt {

    private final static String URL_PROTOCOL = "tcp://";
    private final static String URL_PORT = ":1883";
    private final static String CLIENT_ID = "Mqitty_app_" + System.currentTimeMillis();

    private final MqttAndroidClient mqttClient;
    private final List<MqttMessageListener> listeners = new ArrayList<>();

    public interface MqttMessageListener {
        void onMessageArrived(String topic, String message);
    }

    public Mqtt(Context context, String broker) {
        mqttClient = new MqttAndroidClient(context.getApplicationContext(), getBrokerUrl(broker), CLIENT_ID);
        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Log.d("MQTT", "Connection lost");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                String payload = new String(message.getPayload());
                Log.d("MQTT", "Message arrived: " + payload);
                synchronized (listeners) {
                    for (MqttMessageListener listener : listeners) {
                        listener.onMessageArrived(topic, payload);
                    }
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.d("MQTT", "Delivery complete");
            }
        });
    }

    public void addMessageListener(MqttMessageListener listener) {
        synchronized (listeners) {
            if (!listeners.contains(listener)) {
                listeners.add(listener);
            }
        }
    }

    public void removeMessageListener(MqttMessageListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    public void connect(IMqttActionListener listener) {
        if (mqttClient.isConnected()) {
            if (listener != null) {
                try {
                    listener.onSuccess(null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return;
        }

        MqttConnectOptions connectOptions = new MqttConnectOptions();
        connectOptions.setAutomaticReconnect(true);
        connectOptions.setCleanSession(true);

        try {
            mqttClient.connect(connectOptions, null, listener);
        } catch (MqttException e) {
            Log.e("MQTT", "Connect error", e);
        }
    }

    public boolean isConnected() {
        return mqttClient.isConnected();
    }

    public void subscribe(String topic, int qos) {
        try {
            mqttClient.subscribe(topic, qos);
            Log.d("MQTT", "Subscribed to topic: " + topic);
        } catch (MqttException e) {
            Log.e("MQTT", "Subscribe error", e);
        }
    }

    public boolean publish(String topic, String message) {
        try {
            MqttMessage mqttMessage = new MqttMessage(message.getBytes());
            mqttClient.publish(topic, mqttMessage);
            Log.d("MQTT", "Published: " + message + " to topic: " + topic);
            return true;
        } catch (MqttException e) {
            Log.e("MQTT", "Publish error", e);
            return false;
        }
    }

    public String getBrokerUrl(String broker) {
        return URL_PROTOCOL + broker + URL_PORT;
    }

    public void disconnect() {
        try {
            mqttClient.disconnect();
        } catch (MqttException e) {
            Log.e("MQTT", "Disconnect error", e);
        }
    }
}
