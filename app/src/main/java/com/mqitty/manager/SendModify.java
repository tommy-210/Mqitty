package com.mqitty.manager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
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
    SendModel sendModel;
    DataBaseHelper dataBaseHelper;

    public SendModify(SendModel sendModel) {
        this.sendModel = sendModel;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_modify_send);

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

        save = findViewById(R.id.save_send_btn);
        delete = findViewById(R.id.delete_send_btn);
    }

    private void setComponentsText() {
        name.setText(sendModel.getName());
        description.setText(sendModel.getDescription());
        broker.setText(sendModel.getBroker());
        topic.setText(sendModel.getTopic());
        message.setText(sendModel.getMessage());
    }

    private void addListeners() {
        save.setOnClickListener(v -> {

        });

        delete.setOnClickListener(v -> {
            dataBaseHelper = DataBaseHelper.getInstance(this);

            boolean success = dataBaseHelper.deleteOneSend(sendModel);
            if(success) {
                Toast.makeText(SendModify.this, "Delete Send: " + sendModel.getName(), Toast.LENGTH_SHORT).show();
                returnToMain();
            }else {
                Toast.makeText(SendModify.this, "Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void returnToMain() {
        Intent intent = new Intent(SendModify.this, MainActivity.class);
        startActivity(intent);
    }
}
