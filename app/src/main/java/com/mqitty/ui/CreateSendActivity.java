package com.mqitty.ui;

import static com.mqitty.utils.Utils.*;

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
//        form section
        name = findViewById(R.id.name_msg);
        description = findViewById(R.id.description_msg);
        broker = findViewById(R.id.broker_msg);
        topic = findViewById(R.id.topic_msg);
        message = findViewById(R.id.message_msg);
        create_btn = findViewById(R.id.save_send_btn);
    }

    private void addListeners() {
//        return to main activity
        return_btn.setOnClickListener(v -> returnToMainActivity());
//        create send model
        create_btn.setOnClickListener(v -> {
//            check if input from are correct
            if(!checkInputFormSend(name.getText().toString(), description.getText().toString(), broker.getText().toString(),
                                    topic.getText().toString(), message.getText().toString())) {
                Toast.makeText(CreateSendActivity.this, "Input not valid", Toast.LENGTH_SHORT).show();
                return;
            }
            //create send message model
            dataBaseHelper = DataBaseHelper.getInstance(this);
            SendModel sendModel = new SendModel(-1, name.getText().toString(), description.getText().toString(), broker.getText().toString(),
                                                topic.getText().toString(), message.getText().toString());
//            add send model to db, and check response
            long id = dataBaseHelper.addOneSend(sendModel);
            if (id != -1) {
                sendModel.setId((int) id);
                Toast.makeText(CreateSendActivity.this, "Send creation successfully!", Toast.LENGTH_SHORT).show();
                returnToMainActivity();
            } else {
                Toast.makeText(CreateSendActivity.this, "Error saving", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void returnToMainActivity() {
        startActivity(changeActivity(CreateSendActivity.this, MainActivity.class, EXTRA_PANEL, PANEL_SEND));
        finish();
    }
}
