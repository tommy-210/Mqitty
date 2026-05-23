package com.mqitty.ui;

import android.content.DialogInterface;
import android.content.Intent;
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
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity implements Mqtt.MqttMessageListener {

    ImageView return_btn, send_msg_btn, more_option_btn;
    CheckBox session_on_checkbtn;
    Button clear_session_btn;
    EditText text_input_msg;
    LinearLayout chat_msg_container;
    ScrollView scroll_chat_msg_container;
    SearchView search_view_btn;
    FrameLayout more_option_popup;
    ReceiverModel receiverModel;
    DataBaseHelper dataBaseHelper;
    Mqtt mqtt;
    private final List<String> sentMessagesQueue = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);

//        initialization of databasehelper and receivermodel
        int id = getIntent().getIntExtra("id", -1);
        dataBaseHelper = DataBaseHelper.getInstance(this);
        receiverModel = dataBaseHelper.getReceiverById(id);

        if(receiverModel == null) {
            Toast.makeText(ChatActivity.this, "Error loading data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        dataBaseHelper.createChatTable(receiverModel.getId());

        mqtt = MqttManager.getInstance().getMqtt(ChatActivity.this, receiverModel.getBroker());

        initComponents();

        mqtt.addMessageListener(this);
        mqtt.connect(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                mqtt.subscribe(receiverModel.getTopic(), 1);
                MqttManager.getInstance().addPersistentSubscription(receiverModel.getBroker(), receiverModel.getTopic(), receiverModel.getId());
                runOnUiThread(() -> {
                    session_on_checkbtn.setChecked(true);
                    Toast.makeText(ChatActivity.this, "Connection success", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                runOnUiThread(() -> Toast.makeText(ChatActivity.this, "Connection failed", Toast.LENGTH_SHORT).show());
            }
        });

        reloadAllMessages();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mqtt != null) {
            mqtt.removeMessageListener(this);
        }
    }

    @Override
    public void onMessageArrived(String topic, String message) {
        if (topic.equals(receiverModel.getTopic())) {
            synchronized (sentMessagesQueue) {
                if (sentMessagesQueue.contains(message)) {
                    sentMessagesQueue.remove(message);
                    return;
                }
            }
            MessageModel newMessage = new MessageModel(-1, message, false, System.currentTimeMillis());
            runOnUiThread(() -> addOneMessage(newMessage));
        }
    }

    private void reloadAllMessages() {
        List<MessageModel> messageList = dataBaseHelper.getEveryoneMessageFromChatById(receiverModel.getId());
        reloadMessages(messageList);
    }

    private void reloadMessages(List<MessageModel> messageList) {
        chat_msg_container.removeAllViews();
        for (MessageModel message : messageList) {
            View view;
            if (message.isSend()) {
                view = getLayoutInflater().inflate(R.layout.message_send_model, chat_msg_container, false);
            } else {
                view = getLayoutInflater().inflate(R.layout.message_receive_model, chat_msg_container, false);
            }
            TextView msgText = view.findViewById(R.id.msg_text);
            msgText.setText(message.getMessage());
            chat_msg_container.addView(view);
        }
        scroll_chat_msg_container.post(() -> scroll_chat_msg_container.fullScroll(View.FOCUS_DOWN));
    }

    private void addOneMessage(MessageModel messageModel) {
        View view;
        if (messageModel.isSend()) {
            view = getLayoutInflater().inflate(R.layout.message_send_model, chat_msg_container, false);
        } else {
            view = getLayoutInflater().inflate(R.layout.message_receive_model, chat_msg_container, false);
        }

        TextView msgText = view.findViewById(R.id.msg_text);
        msgText.setText(messageModel.getMessage());

        chat_msg_container.addView(view);
        scroll_chat_msg_container.post(() -> scroll_chat_msg_container.fullScroll(View.FOCUS_DOWN));
    }

    private void initComponents() {
        return_btn = findViewById(R.id.return_btn);
        send_msg_btn = findViewById(R.id.chat_send_msg_btn);
        text_input_msg = findViewById(R.id.chat_input_msg);
        chat_msg_container = findViewById(R.id.chat_msg_container);
        scroll_chat_msg_container = findViewById(R.id.scroll_chat_msg_container);
        more_option_btn = findViewById(R.id.more_option_btn);
        more_option_popup = findViewById(R.id.more_option_popup);
        clear_session_btn = findViewById(R.id.clear_session_btn);
        session_on_checkbtn = findViewById(R.id.start_stop_session);
        session_on_checkbtn.setChecked(MqttManager.getInstance().isSubscribed(receiverModel.getBroker(), receiverModel.getTopic()));
        search_view_btn = findViewById(R.id.search_view);

        return_btn.setOnClickListener(v -> {
//            return to main activity
            Intent intent = new Intent(ChatActivity.this, MainActivity.class);
            intent.putExtra(MainActivity.EXTRA_PANEL, MainActivity.PANEL_RECEIVE);
            startActivity(intent);
        });
        more_option_btn.setOnClickListener(v -> {
            if (more_option_popup.getVisibility() == View.VISIBLE) {
                more_option_popup.setVisibility(View.GONE);
            } else {
                more_option_popup.setVisibility(View.VISIBLE);
            }
        });
        session_on_checkbtn.setOnClickListener(v -> {
            if(session_on_checkbtn.isChecked()) {
                mqtt.connect(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        mqtt.subscribe(receiverModel.getTopic(), 1);
                        MqttManager.getInstance().addPersistentSubscription(receiverModel.getBroker(), receiverModel.getTopic(), receiverModel.getId());
                        runOnUiThread(() -> {
                            session_on_checkbtn.setChecked(true);
                            Toast.makeText(ChatActivity.this, "Connection success", Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        runOnUiThread(() -> {
                            session_on_checkbtn.setChecked(false);
                            Toast.makeText(ChatActivity.this, "Connection failed", Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            }else {
                mqtt.unsubscribe(receiverModel.getTopic());
                MqttManager.getInstance().removePersistentSubscription(receiverModel.getBroker(), receiverModel.getTopic());
            }
        });
        clear_session_btn.setOnClickListener(v -> {
            showConfirmDeleteMsgsDialog();
            reloadAllMessages();
        });
        search_view_btn.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) { return false; }

            @Override
            public boolean onQueryTextSubmit(String query) {
                Toast.makeText(ChatActivity.this, "Filter: " + query, Toast.LENGTH_SHORT).show();
                List<MessageModel> messages = dataBaseHelper.getFilteredMessages(receiverModel.getId(), query);
                reloadMessages(messages);
                return false;
            }
        });
        search_view_btn.setOnCloseListener(() -> {
            reloadAllMessages();
            return false;
        });
        send_msg_btn.setOnClickListener(v -> {
            String msgText = text_input_msg.getText().toString();
            if (msgText.isEmpty()) {
                return;
            }
//            add one message to chat
            MessageModel newMessage = new MessageModel(-1, msgText, true, System.currentTimeMillis());

            synchronized (sentMessagesQueue) {
                sentMessagesQueue.add(msgText);
            }

            boolean mqttSuccess = mqtt.publish(receiverModel.getTopic(), msgText);
            boolean databaseSuccess = dataBaseHelper.addOneMessage(receiverModel.getId(), newMessage);

            if(mqttSuccess && databaseSuccess) {
                text_input_msg.setText("");
                addOneMessage(newMessage);
                Toast.makeText(ChatActivity.this, "Message send:\n" + newMessage.getMessage(), Toast.LENGTH_SHORT).show();
            } else {
                synchronized (sentMessagesQueue) {
                    sentMessagesQueue.remove(msgText);
                }
                Toast.makeText(ChatActivity.this, "Error sending message", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showConfirmDeleteMsgsDialog() throws Resources.NotFoundException {
        new AlertDialog.Builder(this)
                .setTitle("Confirm deletion")
                .setMessage("Do you really want to delete all messages of this chat?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                    Toast.makeText(ChatActivity.this, "Yes", Toast.LENGTH_SHORT).show();
                    dataBaseHelper.deleteAllMessageFromChat(receiverModel.getId());
                })
                .setNegativeButton(android.R.string.no, null).show();
    }
}
