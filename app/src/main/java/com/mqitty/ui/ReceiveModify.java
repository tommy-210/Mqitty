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

public class ReceiveModify  extends AppCompatActivity {

    EditText name, description, broker, topic;
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

        save = findViewById(R.id.save_receiver_btn);
        delete = findViewById(R.id.delete_receive_btn);
        return_btn = findViewById(R.id.return_btn);
    }

    private void setComponentsText() {
        name.setText(receiverModel.getName());
        description.setText(receiverModel.getDescription());
        broker.setText(receiverModel.getBroker());
        topic.setText(receiverModel.getTopic());
    }

    private void addListeners() {
        return_btn.setOnClickListener(v -> returnToMain());

        save.setOnClickListener(v -> {
            if(!checkInputOnSubmit()) {
                Toast.makeText(ReceiveModify.this, "Input not valid", Toast.LENGTH_SHORT).show();
                return;
            }
            
            receiverModel.setName(name.getText().toString());
            receiverModel.setDescription(description.getText().toString());
            receiverModel.setBroker(broker.getText().toString());
            receiverModel.setTopic(topic.getText().toString());

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
        Intent intent = new Intent(ReceiveModify.this, MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_PANEL, MainActivity.PANEL_RECEIVE);
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

        return true;
    }
}
