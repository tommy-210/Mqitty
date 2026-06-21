package com.mqitty.ui;

import static com.mqitty.utils.Utils.*;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.mqitty.MainActivity;
import com.mqitty.R;
import com.mqitty.database.DataBaseHelper;
import com.mqitty.model.MessageModel;
import com.mqitty.model.ReceiverModel;
import com.mqitty.mqtt.Mqtt;
import com.mqitty.mqtt.MqttManager;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import java.util.List;

public class ChatActivity extends AppCompatActivity implements MqttManager.MessageListener {

//    initialization layout elements
    ImageView return_btn, send_msg_btn, more_option_btn;
    CheckBox session_on_check_btn;
    Button clear_session_btn;
    EditText text_input_msg;
    LinearLayout chat_msg_container;
    ScrollView scroll_chat_msg_container;
    SearchView search_view_btn;
    FrameLayout more_option_popup;
//    initialization components
    ReceiverModel receiverModel;
    DataBaseHelper dataBaseHelper;
    Mqtt mqtt;
//    id of current receiver/chat
    public static int currentChatReceiverId = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        create layout
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);

//        initialization of database and receiverModel
        int id = getIntent().getIntExtra("id", -1);
        dataBaseHelper = DataBaseHelper.getInstance(this);
        receiverModel = dataBaseHelper.getReceiverById(id);
        if(receiverModel == null) {
            Toast.makeText(ChatActivity.this, "Error loading data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
//        create table in database for chat
        dataBaseHelper.createChatTable(receiverModel.getId());
//        request mqtt instance of this chat
        mqtt = MqttManager.getInstance().getMqtt(ChatActivity.this, receiverModel.getBroker());

//        initialization components and listeners
        initComponents();
        addListeners();
//        init mqttManager and start connection
        MqttManager.getInstance().addSubscriptionListener(subscriptionListener);
        MqttManager.getInstance().addMessageListener(this);
        mqtt.connect(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                mqtt.subscribe(receiverModel.getTopic(), 1);
                MqttManager.getInstance().addPersistentSubscription(ChatActivity.this, receiverModel.getBroker(), receiverModel.getTopic(), receiverModel.getId());
                runOnUiThread(() -> session_on_check_btn.setChecked(true));
            }
            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                runOnUiThread(() -> Toast.makeText(ChatActivity.this, "Connection failed", Toast.LENGTH_SHORT).show());
            }
        });
        reloadAllMessages();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (receiverModel != null) currentChatReceiverId = receiverModel.getId();
    }

    @Override
    protected void onPause() {
        super.onPause();
        currentChatReceiverId = -1;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MqttManager.getInstance().removeMessageListener(this);
        MqttManager.getInstance().removeSubscriptionListener(subscriptionListener);
    }

    @Override
    public void onMessageArrived(String topic, String message, boolean wasSentByUs) {
//        check if msg arrived is for this chat
        if (topic.equals(receiverModel.getTopic())) {
            if (wasSentByUs) return;
//            create message and add to ui/db
            MessageModel newMessage = new MessageModel(-1, message, false, System.currentTimeMillis());
            runOnUiThread(() -> addOneMessage(newMessage));
        }
    }

    private void initComponents() {
//        header section (return btn, three dots popup)
        return_btn = findViewById(R.id.return_btn);
        more_option_btn = findViewById(R.id.more_option_btn);
        more_option_popup = findViewById(R.id.more_option_popup);
        clear_session_btn = findViewById(R.id.clear_session_btn);
        session_on_check_btn = findViewById(R.id.start_stop_session);
//        main section (contain all messages)
        scroll_chat_msg_container = findViewById(R.id.scroll_chat_msg_container);
        chat_msg_container = findViewById(R.id.chat_msg_container);
//        send msg section
        send_msg_btn = findViewById(R.id.chat_send_msg_btn);
        text_input_msg = findViewById(R.id.chat_input_msg);
        search_view_btn = findViewById(R.id.search_view);
//        initialization ui element
        session_on_check_btn.setChecked(MqttManager.getInstance().isSubscribed(receiverModel.getBroker(), receiverModel.getTopic()));
    }

    private void addListeners() {
//        return to main activity
        return_btn.setOnClickListener(v -> {
            startActivity(changeActivity(ChatActivity.this, MainActivity.class, EXTRA_PANEL, PANEL_RECEIVE));
            finish();
        });
//        enable or disable popup
        more_option_btn.setOnClickListener(v -> {
            more_option_popup.setVisibility(more_option_popup.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        });
//        turn on or off session of chat
        session_on_check_btn.setOnClickListener(v -> {
            if(session_on_check_btn.isChecked()) {
//                initialization connect with mqtt server
                mqtt.connect(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
//                        create stable/persistent connection to server
                        mqtt.subscribe(receiverModel.getTopic(), 1);
                        MqttManager.getInstance().addPersistentSubscription(ChatActivity.this, receiverModel.getBroker(), receiverModel.getTopic(), receiverModel.getId());
                        runOnUiThread(() -> session_on_check_btn.setChecked(true));
                    }
                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        runOnUiThread(() -> {
                            session_on_check_btn.setChecked(false);
                            Toast.makeText(ChatActivity.this, "Connection failed", Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            }else {
//                disconnect mqtt connection
                mqtt.unsubscribe(receiverModel.getTopic());
                MqttManager.getInstance().removePersistentSubscription(receiverModel.getBroker(), receiverModel.getTopic());
            }
        });
//        clear session (remove all messages from chat)
        clear_session_btn.setOnClickListener(v -> {
            showConfirmDeleteMessagesDialog();
            reloadAllMessages();
        });
//        search section to search a specific message
        search_view_btn.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) { return false; }
            @Override
            public boolean onQueryTextSubmit(String query) {
//                reload all filtered messages with query
                reloadMessages(dataBaseHelper.getFilteredMessages(receiverModel.getId(), query));
                return false;
            }
        });
//        remove search filter on close search bar
        search_view_btn.setOnCloseListener(() -> {
            reloadAllMessages();
            return false;
        });
//        send message
        send_msg_btn.setOnClickListener(v -> {
//            handle error (session off, no text)
            if (!session_on_check_btn.isChecked()) {
                Toast.makeText(ChatActivity.this, "Session is off. Please turn it on to send messages.", Toast.LENGTH_SHORT).show();
                return;
            }
            String msgText = text_input_msg.getText().toString();
            if (msgText.isEmpty()) {
                return;
            }
//            add one message to chat
            MessageModel newMessage = new MessageModel(-1, msgText, true, System.currentTimeMillis());
            MqttManager.getInstance().addToSentQueue(msgText);
//            check if mqtt and database added message correctly
            boolean mqttSuccess = mqtt.publish(receiverModel.getTopic(), msgText);
            boolean databaseSuccess = dataBaseHelper.addOneMessage(receiverModel.getId(), newMessage);
            if(mqttSuccess && databaseSuccess) {
                text_input_msg.setText("");
                addOneMessage(newMessage);
                Toast.makeText(ChatActivity.this, "Message send:\n" + newMessage.getMessage(), Toast.LENGTH_SHORT).show();
            } else {
                MqttManager.getInstance().checkAndRemoveFromSentQueue(msgText);
                Toast.makeText(ChatActivity.this, "Error sending message", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private final MqttManager.SubscriptionListener subscriptionListener = count -> {
        runOnUiThread(() -> {
            boolean isSubscribed = MqttManager.getInstance().isSubscribed(receiverModel.getBroker(), receiverModel.getTopic());
            session_on_check_btn.setChecked(isSubscribed);
        });
    };

    private void reloadAllMessages() {
//        reload all messages from db
        reloadMessages(dataBaseHelper.getEveryoneMessageFromChatById(receiverModel.getId()));
    }

    private void reloadMessages(List<MessageModel> messageList) {
//        reload messages from a list
        chat_msg_container.removeAllViews();
        for (MessageModel message : messageList) {
//            create a view (based on message_send_model.xml and message_receive_model.xml)  for each message
            View view;
            if (message.isSend()) {
                view = getLayoutInflater().inflate(R.layout.message_send_model, chat_msg_container, false);
            } else {
                view = getLayoutInflater().inflate(R.layout.message_receive_model, chat_msg_container, false);
            }
//            add all elements of view (message text, timestamp)
            TextView msgText = view.findViewById(R.id.msg_text);
            msgText.setText(message.getMessage());
            msgText.setOnLongClickListener(v -> {
//                add listener on long click to copy message in clipboard of devices
                copyToClipboard(message.getMessage());
                return true;
            });
            TextView msgTime = view.findViewById(R.id.msg_time);
            msgTime.setText(message.getFormattedTimestamp());
            chat_msg_container.addView(view);
        }
//        scroll to end of chat
        scroll_chat_msg_container.post(() -> scroll_chat_msg_container.fullScroll(View.FOCUS_DOWN));
    }

    private void addOneMessage(MessageModel messageModel) {
//        add one message to chat
        View view;
        if (messageModel.isSend()) {
            view = getLayoutInflater().inflate(R.layout.message_send_model, chat_msg_container, false);
        } else {
            view = getLayoutInflater().inflate(R.layout.message_receive_model, chat_msg_container, false);
        }
//        create and set elements of layout
        TextView msgText = view.findViewById(R.id.msg_text);
        msgText.setText(messageModel.getMessage());
        msgText.setOnLongClickListener(v -> {
            copyToClipboard(messageModel.getMessage());
            return true;
        });
        TextView msgTime = view.findViewById(R.id.msg_time);
        msgTime.setText(messageModel.getFormattedTimestamp());

        chat_msg_container.addView(view);
        scroll_chat_msg_container.post(() -> scroll_chat_msg_container.fullScroll(View.FOCUS_DOWN));
    }

    private void showConfirmDeleteMessagesDialog() throws Resources.NotFoundException {
        new AlertDialog.Builder(this)
                .setTitle("Confirm deletion")
                .setMessage("Do you really want to delete all messages of this chat?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
//                    delete all messages from db and clear chat ui
                    Toast.makeText(ChatActivity.this, "Yes", Toast.LENGTH_SHORT).show();
                    dataBaseHelper.deleteAllMessageFromChat(receiverModel.getId());
                    chat_msg_container.removeAllViews();
                })
                .setNegativeButton(android.R.string.no, null).show();
    }

    private void copyToClipboard(String text) {
//        copy text into clipboard of device
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Copied Message", text);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Message copied to clipboard", Toast.LENGTH_SHORT).show();
        }
    }
}
