package com.mqitty;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    ImageView send_btn, receive_btn, settings_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        send_btn = findViewById(R.id.send_btn);
        receive_btn = findViewById(R.id.receive_btn);
        settings_btn = findViewById(R.id.settings_btn);

        send_btn.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "send", Toast.LENGTH_SHORT).show();
            System.out.println("send panel");
        });
        receive_btn.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "receive", Toast.LENGTH_SHORT).show();
            System.out.println("receive panel");
        });
        settings_btn.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "settings", Toast.LENGTH_SHORT).show();
            System.out.println("settings panel");
        });
    }
}