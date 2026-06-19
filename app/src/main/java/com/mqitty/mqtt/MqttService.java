package com.mqitty.mqtt;

import static com.mqitty.utils.Utils.*;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.widget.RemoteViews;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.mqitty.MainActivity;
import com.mqitty.R;
import com.mqitty.database.DataBaseHelper;
import com.mqitty.model.ReceiverModel;
import com.mqitty.ui.ChatActivity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MqttService extends Service {

    DataBaseHelper db;

    private static final String CHANNEL_ID = "MqttServiceChannel";
    private static final String MESSAGE_CHANNEL_ID = "MqttMessageChannel";
    private static final String GROUP_KEY = "com.mqitty.mqtt.NOTIFICATION_GROUP";
    private static final int NOTIFICATION_ID = 1;
    private static final int SUMMARY_ID = 2;
    public static final String ACTION_STOP_SERVICE = "STOP_MQTT_SERVICE";
    public static final String ACTION_DISMISS_NOTIFICATION = "DISMISS_NOTIFICATION";

    private final Map<Integer, List<String>> messageHistory = new HashMap<>();

    private final MqttManager.SubscriptionListener listener = count -> {
        if (count == 0) {
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.cancel(SUMMARY_ID);
            }
            stopForeground(true);
            stopSelf();
        } else {
            updateNotification(count);
        }
    };

    private final MqttManager.MessageListener messageListener = (topic, message, wasSentByUs) -> {
        if (!wasSentByUs) {
            List<Integer> ids = MqttManager.getInstance().getReceiverIdsForTopic(topic);
            for (int id : ids) {
                if (id == ChatActivity.currentChatReceiverId) continue;
                ReceiverModel receiver = MqttManager.getInstance().getReceiverById(id);
                if (receiver != null && checkNotificationType(receiver, message)) {
                    showIncomingMessageNotification(receiver, message);
                }
            }
        }
    };

    private boolean checkNotificationType(ReceiverModel receiver, String message) {
        int type = receiver.getNotificationType();
        if (type == 1) {
            return false;
        } else if (type == 2) {
            return true;
        } else if (type == CUSTOM_NOTIFICATION) {
            String keyword = receiver.getKeywordCustomNotification();
            return keyword != null && !keyword.isEmpty() && message.contains(keyword);
        }
        return true;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();

        MqttManager.getInstance().addSubscriptionListener(listener);

        db = DataBaseHelper.getInstance(this);
        boolean isNotificationEnableInGeneralSettings = Boolean.parseBoolean(db.getSettingByLabel(DataBaseHelper.SettingsDB.NOTIFICATION_ENABLE));
        if(isNotificationEnableInGeneralSettings){
            MqttManager.getInstance().addMessageListener(messageListener);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_STOP_SERVICE.equals(action)) {
                MqttManager.getInstance().stopAllSubscriptions();
                NotificationManager manager = getSystemService(NotificationManager.class);
                if (manager != null) {
                    manager.cancel(SUMMARY_ID);
                }
                stopForeground(true);
                stopSelf();
                return START_NOT_STICKY;
            } else if (ACTION_DISMISS_NOTIFICATION.equals(action)) {
                int receiverId = intent.getIntExtra(EXTRA_ELEMENT_ID, -1);
                if (receiverId != -1) {
                    messageHistory.remove(receiverId);
                    updateGroupSummary();
                }
                return START_NOT_STICKY;
            }
        }

        int count = MqttManager.getInstance().getActiveSubscriptionCount();
        if (count > 0) {
            startForeground(NOTIFICATION_ID, showReceiverListeningNotification(count));
            updateGroupSummary();
            return START_STICKY;
        } else {
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.cancel(SUMMARY_ID);
            }
            stopSelf();
            return START_NOT_STICKY;
        }
    }

    private void updateNotification(int count) {
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, showReceiverListeningNotification(count));
            updateGroupSummary();
        }
    }

    private void updateGroupSummary() {
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager == null) return;

        int messageCount = 0;
        for (List<String> history : messageHistory.values()) {
            messageCount += history.size();
        }

        String contentText = "Mqitty Service is running";
        if (messageCount > 0) {
            contentText += " • " + messageCount + " new message" + (messageCount > 1 ? "s" : "");
        }

        Intent notificationIntent = changeActivity(MqttService.this, MainActivity.class, EXTRA_PANEL, PANEL_RECEIVE);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification summary = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Mqitty")
                .setContentText(contentText)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setGroup(GROUP_KEY)
                .setGroupSummary(true)
                .setAutoCancel(false)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent)
                .build();

        manager.notify(SUMMARY_ID, summary);
    }

    private Notification showReceiverListeningNotification(int count) {
        Intent notificationIntent = changeActivity(MqttService.this, MainActivity.class, EXTRA_PANEL, PANEL_RECEIVE);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        Intent stopIntent = new Intent(this, MqttService.class);
        stopIntent.setAction(ACTION_STOP_SERVICE);
        PendingIntent stopPendingIntent = PendingIntent.getService(this,
                0, stopIntent, PendingIntent.FLAG_IMMUTABLE);

        List<String> names = MqttManager.getInstance().getActiveReceiverNames();
        String actionText = "Stop";

        String contentText = "";
        if (!names.isEmpty()) {
            if(names.size() > 1) {
                actionText += " All";
                contentText = count + " receivers listening\nFrom: " + String.join(", ", names);
            }else {
                contentText = names.get(0) + " is listening";
            }
        }

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Mqitty - MQTT Service")
                .setContentText(contentText)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setGroup(GROUP_KEY)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, actionText, stopPendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText))
                .build();
    }

    private void showIncomingMessageNotification(ReceiverModel receiver, String message) {
        int notificationId = 1000 + receiver.getId();
        NotificationManager manager = getSystemService(NotificationManager.class);

        boolean isActive = false;
        if (manager == null) {
            return;
        }
        for (android.service.notification.StatusBarNotification sbn : manager.getActiveNotifications()) {
            if (sbn.getId() == notificationId) {
                isActive = true;
                break;
            }
        }

        List<String> history = messageHistory.getOrDefault(receiver.getId(), new ArrayList<>());
        if (!isActive) {
            assert history != null;
            history.clear();
        }
        assert history != null;
        history.add(message);
        messageHistory.put(receiver.getId(), history);

        String summaryText = history.size() > 1 ? history.size() + " new messages" : message;

        RemoteViews smallView = new RemoteViews(getPackageName(), R.layout.notification_small);
        smallView.setTextViewText(R.id.notification_title, receiver.getName());
        smallView.setTextViewText(R.id.notification_info, summaryText);

        RemoteViews largeView = new RemoteViews(getPackageName(), R.layout.notification_large);
        largeView.setTextViewText(R.id.notification_title, receiver.getName());
        largeView.setTextViewText(R.id.notification_info, history.size() > 1 ? summaryText : "one new message");

        largeView.removeAllViews(R.id.messages_container);
        for (String msg : history) {
            RemoteViews messageItem = new RemoteViews(getPackageName(), R.layout.notification_message_item);
            messageItem.setTextViewText(R.id.message_text, msg);
            largeView.addView(R.id.messages_container, messageItem);
        }

        Intent chatIntent = changeActivity(this, ChatActivity.class, EXTRA_ELEMENT_ID, receiver.getId());
        chatIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                receiver.getId(), chatIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Intent deleteIntent = new Intent(this, MqttService.class);
        deleteIntent.setAction(ACTION_DISMISS_NOTIFICATION);
        deleteIntent.putExtra(EXTRA_ELEMENT_ID, receiver.getId());
        PendingIntent deletePendingIntent = PendingIntent.getService(this,
                receiver.getId(), deleteIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, MESSAGE_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setDeleteIntent(deletePendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setGroup(GROUP_KEY)
                .setCustomContentView(smallView)
                .setCustomBigContentView(largeView)
                .build();

        manager.notify(notificationId, notification);
        updateGroupSummary();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "MQTT Service Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationChannel messageChannel = new NotificationChannel(
                    MESSAGE_CHANNEL_ID,
                    "MQTT Messages",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
                manager.createNotificationChannel(messageChannel);
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
        MqttManager.getInstance().removeMessageListener(messageListener);
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.cancel(SUMMARY_ID);
        }
    }
}
