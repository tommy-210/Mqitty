package com.mqitty;

import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    ImageView send_btn, receive_btn, settings_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        send_btn = findViewById(R.id.send_btn);
        receive_btn = findViewById(R.id.receive_btn);
        settings_btn = findViewById(R.id.settings_btn);

        send_btn.setOnClickListener(v -> {
            System.out.println("send panel");
        });
        receive_btn.setOnClickListener(v -> {
            System.out.println("receive panel");
        });
        settings_btn.setOnClickListener(v -> {
            System.out.println("settings panel");
        });
    }
}