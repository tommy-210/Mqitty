package com.mqitty.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.mqitty.MainActivity;
import com.mqitty.R;
import com.mqitty.database.DataBaseHelper;
import com.mqitty.model.ReceiverModel;
import static com.mqitty.utils.Utils.*;

public class CreateReceiveActivity extends AppCompatActivity {

    ImageView return_btn;
    Button create_btn;
    EditText name, description, broker, topic, keywordCustomNotification;
    RadioGroup notificationType;
    DataBaseHelper dataBaseHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_receiver);

        initComponents();

        addListeners();
    }

    private void initComponents() {
        return_btn = findViewById(R.id.return_btn);
        create_btn = findViewById(R.id.save_receiver_btn);
        name = findViewById(R.id.name_receiver);
        description = findViewById(R.id.description_receiver);
        broker = findViewById(R.id.broker_receiver);
        topic = findViewById(R.id.topic_receiver);
        notificationType = findViewById(R.id.radioGroup_notification_receiver);
        keywordCustomNotification = findViewById(R.id.keyword_notification_receiver);

//        set keyword edit text disable and active it only if user selected custom in radio btn
        keywordCustomNotification.setFocusable(false);
    }

    private void addListeners() {
        return_btn.setOnClickListener(v -> {
            startActivity(changeActivity(CreateReceiveActivity.this, MainActivity.class, EXTRA_PANEL, PANEL_RECEIVE));
            finish();
        });

//        activate or not keyword edit text only if custom radio button is selected
        notificationType.setOnCheckedChangeListener((group, checkedId) -> {
            keywordCustomNotification.setFocusable(checkedId == CUSTOM_NOTIFICATION);
            Toast.makeText(CreateReceiveActivity.this, "notif: " + checkedId, Toast.LENGTH_SHORT).show();
        });

        create_btn.setOnClickListener(v -> {
            int checkedId = notificationType.getCheckedRadioButtonId();
            int type = 1;
            if (checkedId == R.id.radioBtn_all_receiver) type = 2;
            else if (checkedId == R.id.radioBtn_custom_receiver) type = CUSTOM_NOTIFICATION;

            if(!checkInputFormReceive(name.getText().toString(), description.getText().toString(), broker.getText().toString(),
                    topic.getText().toString(), type, keywordCustomNotification.getText().toString())) {
                Toast.makeText(CreateReceiveActivity.this, "Input not valid", Toast.LENGTH_SHORT).show();
                return;
            }

            dataBaseHelper = DataBaseHelper.getInstance(this);

//            create send message model
            ReceiverModel receiverModel = new ReceiverModel(-1, name.getText().toString(), description.getText().toString(), broker.getText().toString(),
                    topic.getText().toString(), notificationType.getCheckedRadioButtonId(), keywordCustomNotification.getText().toString());

            long id = dataBaseHelper.addOneReceiver(receiverModel);
            if(id != -1) {
                receiverModel.setId((int) id);
                Toast.makeText(CreateReceiveActivity.this, receiverModel.toString(), Toast.LENGTH_SHORT).show();

//               create a chat table in database for messages
                dataBaseHelper.createChatTable(receiverModel.getId());

                startActivity(changeActivity(CreateReceiveActivity.this, MainActivity.class, EXTRA_PANEL, PANEL_RECEIVE));
                finish();
            }else {
                Toast.makeText(CreateReceiveActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
