package com.mqitty.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.mqitty.MainActivity;
import com.mqitty.R;
import com.mqitty.database.DataBaseHelper;
import com.mqitty.model.MessageModel;
import com.mqitty.model.ReceiverModel;

import java.util.List;

public class ChatActivity extends AppCompatActivity {

    ImageView return_btn, send_msg_btn;
    EditText text_input_msg;
    LinearLayout chat_msg_container;
    ScrollView scroll_chat_msg_container;
    ReceiverModel receiverModel;
    DataBaseHelper dataBaseHelper;

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

        initComponents();
        reloadAllMessages();
    }

    private void reloadAllMessages() {
        chat_msg_container.removeAllViews();
        List<MessageModel> messageList = dataBaseHelper.getEveryoneMessageFromChatById(receiverModel.getId());
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
        View view = getLayoutInflater().inflate(R.layout.message_send_model, chat_msg_container, false);

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

        return_btn.setOnClickListener(v -> {
//            return to main activity
            Intent intent = new Intent(ChatActivity.this, MainActivity.class);
            startActivity(intent);
        });
        send_msg_btn.setOnClickListener(v -> {
            String msgText = text_input_msg.getText().toString();
            if (msgText.isEmpty()) {
                return;
            }
//            add one message to chat
            MessageModel newMessage = new MessageModel(-1, msgText, true, System.currentTimeMillis());
            if (dataBaseHelper.addOneMessage(receiverModel.getId(), newMessage)) {
                text_input_msg.setText("");
                addOneMessage(newMessage);
                Toast.makeText(ChatActivity.this, "Message send:\n" + newMessage.getMessage(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ChatActivity.this, "Error sending message", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
