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

public class CreateSendActivity extends AppCompatActivity {

    ImageView return_btn;
    Button create_btn;
    EditText name, description, broker, topic, message;
    DataBaseHelper dataBaseHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_send);

        initComponents();

        addListeners();
    }

    private void initComponents() {
        return_btn = findViewById(R.id.return_btn);
        create_btn = findViewById(R.id.save_send_btn);
        name = findViewById(R.id.name_msg);
        description = findViewById(R.id.description_msg);
        broker = findViewById(R.id.broker_msg);
        topic = findViewById(R.id.topic_msg);
        message = findViewById(R.id.message_msg);
    }

    private void addListeners() {
        return_btn.setOnClickListener(v -> {
            returnToMainActivity();
        });

        create_btn.setOnClickListener(v -> {

            if(!checkInputOnSubmit()) {
                Toast.makeText(CreateSendActivity.this, "Input not valid", Toast.LENGTH_SHORT).show();
                return;
            }

            dataBaseHelper = DataBaseHelper.getInstance(this);

            //create send message model
            SendModel sendModel = new SendModel(-1, name.getText().toString(), description.getText().toString(), broker.getText().toString(),
                                                topic.getText().toString(), message.getText().toString());
            
            boolean success = dataBaseHelper.addOneSend(sendModel);
            if (success) {
                Toast.makeText(CreateSendActivity.this, "Success!", Toast.LENGTH_SHORT).show();
                returnToMainActivity();
            } else {
                Toast.makeText(CreateSendActivity.this, "Error saving", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void returnToMainActivity() {
        Intent intent = new Intent(CreateSendActivity.this, MainActivity.class);
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
