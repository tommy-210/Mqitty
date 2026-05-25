package com.mqitty.mqtt;

import static com.mqitty.utils.Utils.*;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.mqitty.MainActivity;
import com.mqitty.R;

import java.util.List;

public class MqttService extends Service {
    private static final String CHANNEL_ID = "MqttServiceChannel";
    private static final int NOTIFICATION_ID = 1;
    public static final String ACTION_STOP_SERVICE = "STOP_MQTT_SERVICE";

    private final MqttManager.SubscriptionListener listener = count -> {
        if (count == 0) {
            stopForeground(true);
            stopSelf();
        } else {
            updateNotification(count);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        MqttManager.getInstance().addSubscriptionListener(listener);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_STOP_SERVICE.equals(intent.getAction())) {
            MqttManager.getInstance().stopAllSubscriptions();
            stopForeground(true);
            stopSelf();
            return START_NOT_STICKY;
        }

        int count = MqttManager.getInstance().getActiveSubscriptionCount();
        if (count > 0) {
            startForeground(NOTIFICATION_ID, getNotification(count));
        } else {
            stopSelf();
        }
        return START_STICKY;
    }

    private void updateNotification(int count) {
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, getNotification(count));
        }
    }

    private Notification getNotification(int count) {
        Intent notificationIntent = changeActivity(MqttService.this, MainActivity.class, EXTRA_PANEL, PANEL_RECEIVE);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        Intent stopIntent = new Intent(this, MqttService.class);
        stopIntent.setAction(ACTION_STOP_SERVICE);
        PendingIntent stopPendingIntent = PendingIntent.getService(this,
                0, stopIntent, PendingIntent.FLAG_IMMUTABLE);

        List<String> names = MqttManager.getInstance().getActiveReceiverNames();
        String contentText = count + " receiver(s) listening";
        if (!names.isEmpty()) {
            contentText += "\nFrom: " + String.join(", ", names);
        }

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Mqitty - MQTT Service")
                .setContentText(contentText)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop All", stopPendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText))
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "MQTT Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MqttManager.getInstance().removeSubscriptionListener(listener);
    }
}
