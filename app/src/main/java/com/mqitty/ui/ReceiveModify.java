package com.mqitty.ui;

import static com.mqitty.utils.Utils.*;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.mqitty.MainActivity;
import com.mqitty.R;
import com.mqitty.database.DataBaseHelper;
import com.mqitty.model.ReceiverModel;

import java.util.concurrent.atomic.AtomicInteger;

public class ReceiveModify  extends AppCompatActivity {

    EditText name, description, broker, topic, keywordCustomNotification;
    RadioGroup notificationType;
    RadioButton noNotificationBtn, allNotificationBtn, customNotificationBtn;
    Button save, delete;
    ImageView return_btn;
    ReceiverModel receiverModel;
    DataBaseHelper dataBaseHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_modify_receiver);

        int id = getIntent().getIntExtra("id", -1);
        dataBaseHelper = DataBaseHelper.getInstance(this);
        receiverModel = dataBaseHelper.getReceiverById(id);

        if (receiverModel == null) {
            Toast.makeText(this, "Error loading data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initComponents();

        setComponentsText();

        addListeners();
    }

    private void initComponents() {
        name = findViewById(R.id.name_receiver);
        description = findViewById(R.id.description_receiver);
        broker = findViewById(R.id.broker_receiver);
        topic = findViewById(R.id.topic_receiver);
        notificationType = findViewById(R.id.radioGroup_notification_receiver);
        noNotificationBtn = findViewById(R.id.radioBtn_none_receiver);
        allNotificationBtn = findViewById(R.id.radioBtn_all_receiver);
        customNotificationBtn = findViewById(R.id.radioBtn_custom_receiver);
        keywordCustomNotification = findViewById(R.id.keyword_notification_receiver);

        save = findViewById(R.id.save_receiver_btn);
        delete = findViewById(R.id.delete_receive_btn);
        return_btn = findViewById(R.id.return_btn);
    }

    private void setComponentsText() {
        name.setText(receiverModel.getName());
        description.setText(receiverModel.getDescription());
        broker.setText(receiverModel.getBroker());
        topic.setText(receiverModel.getTopic());

        notificationType.check(getRadioBtnId(receiverModel.getNotificationType()));
        keywordCustomNotification.setText(receiverModel.getKeywordCustomNotification());
        keywordCustomNotification.setEnabled(customNotificationBtn.isChecked());
    }

    private void addListeners() {
        return_btn.setOnClickListener(v -> returnToMain());

//        activate or not keyword edit text only if custom radio button is selected
        notificationType.setOnCheckedChangeListener((group, checkedId) ->
                keywordCustomNotification.setEnabled(checkedId == R.id.radioBtn_custom_receiver));

        save.setOnClickListener(v -> {
            int typeNotification = -1;
            if(noNotificationBtn.isChecked()) typeNotification = NO_NOTIFICATION;
            else if(allNotificationBtn.isChecked()) typeNotification = ALL_NOTIFICATION;
            else if(customNotificationBtn.isChecked()) typeNotification = CUSTOM_NOTIFICATION;

            if(!checkInputFormReceive(name.getText().toString(), description.getText().toString(), broker.getText().toString(),
                    topic.getText().toString(), typeNotification, keywordCustomNotification.getText().toString())) {
                Toast.makeText(ReceiveModify.this, "Input not valid", Toast.LENGTH_SHORT).show();
                return;
            }

            receiverModel.setName(name.getText().toString());
            receiverModel.setDescription(description.getText().toString());
            receiverModel.setBroker(broker.getText().toString());
            receiverModel.setTopic(topic.getText().toString());
            receiverModel.setNotificationType(typeNotification);
            receiverModel.setKeywordCustomNotification(keywordCustomNotification.getText().toString());

            boolean success = dataBaseHelper.updateOneReceiver(receiverModel);
            if(success) {
                Toast.makeText(ReceiveModify.this, "Update: " + receiverModel.getName(), Toast.LENGTH_SHORT).show();
                returnToMain();
            }else {
                Toast.makeText(ReceiveModify.this, "Error", Toast.LENGTH_SHORT).show();
            }
        });

        delete.setOnClickListener(v -> {
            boolean success = dataBaseHelper.deleteOneReceiver(receiverModel);
            if(success) {
                Toast.makeText(ReceiveModify.this, "Delete: " + receiverModel.getName(), Toast.LENGTH_SHORT).show();
//                delete chat table
                dataBaseHelper.removeChaTable(receiverModel.getId());
                returnToMain();
            }else {
                Toast.makeText(ReceiveModify.this, "Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void returnToMain() {
        startActivity(changeActivity(ReceiveModify.this, MainActivity.class, EXTRA_PANEL, PANEL_RECEIVE));
        finish();
    }

    private int getRadioBtnId(int value) {
        if(value == NO_NOTIFICATION) return noNotificationBtn.getId();
        else if(value == ALL_NOTIFICATION) return allNotificationBtn.getId();
        else if(value == CUSTOM_NOTIFICATION) return customNotificationBtn.getId();
        return -1;
    }
}
