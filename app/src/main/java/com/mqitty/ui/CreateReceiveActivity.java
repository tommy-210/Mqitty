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
import com.mqitty.model.ReceiverModel;
import com.mqitty.model.SendModel;

public class CreateReceiveActivity extends AppCompatActivity {

    ImageView return_btn;
    Button create_btn;
    EditText name, description, broker, topic;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_receiver);

        return_btn = findViewById(R.id.return_btn);
        create_btn = findViewById(R.id.save_receiver_btn);

        return_btn.setOnClickListener(v -> {
            Intent intent = new Intent(CreateReceiveActivity.this, MainActivity.class);
            startActivity(intent);
        });

        create_btn.setOnClickListener(v -> {
//            create send message model
            ReceiverModel receiverModel = new ReceiverModel(-1, name.getText().toString(), description.getText().toString(),
                                        broker.getText().toString(), topic.getText().toString());
            Toast.makeText(CreateReceiveActivity.this, receiverModel.toString(), Toast.LENGTH_SHORT).show();

            returnToMainActivity();
        });
    }

    private void returnToMainActivity() {
        Intent intent = new Intent(CreateReceiveActivity.this, MainActivity.class);
        startActivity(intent);
    }
}
