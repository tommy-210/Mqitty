package com.mqitty.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.mqitty.MainActivity;
import com.mqitty.R;
import com.mqitty.database.DataBaseHelper;
import com.mqitty.model.SendModel;

public class SendModify extends AppCompatActivity {

    EditText name, description, broker, topic, message;
    Button save, delete;
    ImageView return_btn;
    SendModel sendModel;
    DataBaseHelper dataBaseHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_modify_send);

        int id = getIntent().getIntExtra("id", -1);
        dataBaseHelper = DataBaseHelper.getInstance(this);
        sendModel = dataBaseHelper.getSendById(id);

        if (sendModel == null) {
            Toast.makeText(this, "Error loading data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initComponents();

        setComponentsText();

        addListeners();
    }

    private void initComponents() {
        name = findViewById(R.id.name_msg);
        description = findViewById(R.id.description_msg);
        broker = findViewById(R.id.broker_msg);
        topic = findViewById(R.id.topic_msg);
        message = findViewById(R.id.message_msg);

        save = findViewById(R.id.modify_send_btn);
        delete = findViewById(R.id.delete_send_btn);
        return_btn = findViewById(R.id.return_btn);
    }

    private void setComponentsText() {
        name.setText(sendModel.getName());
        description.setText(sendModel.getDescription());
        broker.setText(sendModel.getBroker());
        topic.setText(sendModel.getTopic());
        message.setText(sendModel.getMessage());
    }

    private void addListeners() {
        return_btn.setOnClickListener(v -> returnToMain());

        save.setOnClickListener(v -> {
            if(!checkInputOnSubmit()) {
                Toast.makeText(SendModify.this, "Input not valid", Toast.LENGTH_SHORT).show();
                return;
            }
            
            sendModel.setName(name.getText().toString());
            sendModel.setDescription(description.getText().toString());
            sendModel.setBroker(broker.getText().toString());
            sendModel.setTopic(topic.getText().toString());
            sendModel.setMessage(message.getText().toString());

            boolean success = dataBaseHelper.updateOneSend(sendModel);
            if(success) {
                Toast.makeText(SendModify.this, "Update: " + sendModel.getName(), Toast.LENGTH_SHORT).show();
                returnToMain();
            }else {
                Toast.makeText(SendModify.this, "Error", Toast.LENGTH_SHORT).show();
            }
        });

        delete.setOnClickListener(v -> {
            boolean success = dataBaseHelper.deleteOneSend(sendModel);
            if(success) {
                Toast.makeText(SendModify.this, "Delete: " + sendModel.getName(), Toast.LENGTH_SHORT).show();
                returnToMain();
            }else {
                Toast.makeText(SendModify.this, "Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void returnToMain() {
        Intent intent = new Intent(SendModify.this, MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_PANEL, MainActivity.PANEL_SEND);
        startActivity(intent);
    }

    private boolean checkInputOnSubmit() {
//        check name and description for gui
        if(name.getText().toString().isBlank() || description.getText().toString().isBlank()) {
            return false;
        }
//        check input for mqtt
        if(broker.getText().toString().isBlank() || topic.getText().toString().isBlank()) {
            return false;
        }

//        check message
        return !message.getText().toString().isBlank();
    }
}
