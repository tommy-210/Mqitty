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
import com.mqitty.model.ReceiverModel;
import com.mqitty.model.SendModel;

public class CreateReceiveActivity extends AppCompatActivity {

    ImageView return_btn;
    Button create_btn;
    EditText name, description, broker, topic;
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
    }

    private void addListeners() {
        return_btn.setOnClickListener(v -> {
            Intent intent = new Intent(CreateReceiveActivity.this, MainActivity.class);
            intent.putExtra(MainActivity.EXTRA_PANEL, MainActivity.PANEL_RECEIVE);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        create_btn.setOnClickListener(v -> {

            if(!checkInputOnSubmit()) {
                Toast.makeText(CreateReceiveActivity.this, "Input not valid", Toast.LENGTH_SHORT).show();
                return;
            }

            dataBaseHelper = DataBaseHelper.getInstance(this);

//            create send message model
            ReceiverModel receiverModel = new ReceiverModel(-1, name.getText().toString(), description.getText().toString(),
                    broker.getText().toString(), topic.getText().toString());

            long id = dataBaseHelper.addOneReceiver(receiverModel);
            if(id != -1) {
                receiverModel.setId((int) id);
                Toast.makeText(CreateReceiveActivity.this, receiverModel.toString(), Toast.LENGTH_SHORT).show();

//               create a chat table in database for messages
                dataBaseHelper.createChatTable(receiverModel.getId());

                returnToMainActivity();
            }else {
                Toast.makeText(CreateReceiveActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void returnToMainActivity() {
        Intent intent = new Intent(CreateReceiveActivity.this, MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_PANEL, MainActivity.PANEL_RECEIVE);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
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

        return true;
    }
}
