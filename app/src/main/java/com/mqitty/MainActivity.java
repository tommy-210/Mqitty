package com.mqitty;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.mqitty.ui.CreateReceiveActivity;
import com.mqitty.ui.CreateSendActivity;

public class MainActivity extends AppCompatActivity {

    ImageView send_btn, receive_btn, settings_btn;
    ViewGroup panelContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        initComponents();
        componentListener();
        
        // Initial setup for the default included layout
        setupInnerPanelListeners();
    }

    private void initComponents() {
        send_btn = findViewById(R.id.send_btn);
        receive_btn = findViewById(R.id.receive_btn);
        settings_btn = findViewById(R.id.settings_btn);
        panelContainer = findViewById(R.id.panel_include_main_activity);
    }

    private void componentListener() {
        send_btn.setOnClickListener(v -> {
            showPanel(R.layout.activity_send);
        });
        receive_btn.setOnClickListener(v -> {
            showPanel(R.layout.activity_receive);
        });
        settings_btn.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "settings", Toast.LENGTH_SHORT).show();
        });
    }

    private void showPanel(int layoutResId) {
        // Clear current views in the container
        panelContainer.removeAllViews();

        // Inflate the new layout and add it to the container
        LayoutInflater.from(this).inflate(layoutResId, panelContainer, true);
        
        // Re-setup listeners for buttons inside the newly inflated layout
        setupInnerPanelListeners();
    }

    private void setupInnerPanelListeners() {
        View addSendBtn = findViewById(R.id.add_send_btn);
        if (addSendBtn != null) {
            addSendBtn.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, CreateSendActivity.class);
                startActivity(intent);
            });
        }

        View addReceiveBtn = findViewById(R.id.add_receive_btn);
        if (addReceiveBtn != null) {
            addReceiveBtn.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, CreateReceiveActivity.class);
                startActivity(intent);
            });
        }
    }
}
