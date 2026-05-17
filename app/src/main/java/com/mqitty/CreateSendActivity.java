package com.mqitty;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class CreateSendActivity extends AppCompatActivity {

    ImageView return_btn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_send);

        return_btn = findViewById(R.id.return_btn);

        return_btn.setOnClickListener(v -> {
            Toast.makeText(CreateSendActivity.this, "return", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(CreateSendActivity.this, MainActivity.class);
            startActivity(intent);
        });
    }
}
