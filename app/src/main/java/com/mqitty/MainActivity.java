package com.mqitty;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.mqitty.database.DataBaseHelper;
import com.mqitty.manager.ReceiveManager;
import com.mqitty.manager.SendModify;
import com.mqitty.model.ReceiverModel;
import com.mqitty.model.SendModel;
import com.mqitty.ui.CreateReceiveActivity;
import com.mqitty.ui.CreateSendActivity;
import com.mqitty.manager.SendManager;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ImageView send_btn, receive_btn, settings_btn;
    ViewGroup sendPanelContainer, receivePanelContainer;
    DataBaseHelper dataBaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        dataBaseHelper = DataBaseHelper.getInstance(this);

        initComponents();
        componentListener();
        
        // Initial setup for the default included layout
        setupInnerPanelListeners();
    }

    private void initComponents() {
        send_btn = findViewById(R.id.send_btn);
        receive_btn = findViewById(R.id.receive_btn);
        settings_btn = findViewById(R.id.settings_btn);
        //main container
        sendPanelContainer = findViewById(R.id.send_panel_container);
        receivePanelContainer = findViewById(R.id.receive_panel_container);

        // Pre-inflate layouts into containers
        LayoutInflater.from(this).inflate(R.layout.activity_send, sendPanelContainer, true);
        LayoutInflater.from(this).inflate(R.layout.activity_receive, receivePanelContainer, true);

        refreshSendPanelData();
        refreshReceivePanelData();
    }

    private void componentListener() {
        send_btn.setOnClickListener(v -> {
            showPanel(sendPanelContainer);
            refreshSendPanelData();
        });
        receive_btn.setOnClickListener(v -> {
            showPanel(receivePanelContainer);
            refreshReceivePanelData();
        });
        settings_btn.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "settings", Toast.LENGTH_SHORT).show();
        });
    }

    private void showPanel(ViewGroup panelToShow) {
        sendPanelContainer.setVisibility(View.GONE);
        receivePanelContainer.setVisibility(View.GONE);

        panelToShow.setVisibility(View.VISIBLE);
    }

    private void setupInnerPanelListeners() {
        //add custom send msg
        View addSendBtn = findViewById(R.id.add_send_btn);
        if (addSendBtn != null) {
            addSendBtn.setOnClickListener(v -> {
                changeActivity(CreateSendActivity.class);
            });
        }
        
        //add custom receiver
        View addReceiveBtn = findViewById(R.id.add_receive_btn);
        if (addReceiveBtn != null) {
            addReceiveBtn.setOnClickListener(v -> {
                changeActivity(CreateReceiveActivity.class);
            });
        }
    }

    private void refreshSendPanelData() {
        List<SendModel> sendModels = dataBaseHelper.getEveryoneSend();
        View sendRoot = findViewById(R.id.activity_send_root);
        if (sendRoot instanceof ViewGroup) {
            ViewGroup container = (ViewGroup) sendRoot;
            container.removeViews(0, container.getChildCount() - 1);
            // The panel was just inflated, so we add all stored models
            for (SendModel model : sendModels) {
                View view = SendManager.addSendModelToLayout(container, model);
                if (view != null) {
                    view.setOnClickListener(v -> {
                        Toast.makeText(MainActivity.this, "Send: " + model.getName(), Toast.LENGTH_SHORT).show();
                        changeActivity(SendModify.class);
                        new SendModify(model);
                    });
                }
            }
        }
    }

    private void refreshReceivePanelData() {
        List<ReceiverModel> receiverModels = dataBaseHelper.getEveryoneReceiver();
        View sendRoot = findViewById(R.id.activity_receive_root);
        if (sendRoot instanceof ViewGroup) {
            ViewGroup container = (ViewGroup) sendRoot;
            container.removeViews(0, container.getChildCount() - 1);
            // The panel was just inflated, so we add all stored models
            for (ReceiverModel model : receiverModels) {
                View view = ReceiveManager.addReceiveModelToLayout(container, model);
                if (view != null) {
                    view.setOnClickListener(v -> {
                        Toast.makeText(MainActivity.this, "Receive: " + model.getName(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        }
    }

    public void changeActivity(Class toClass) {
        Intent intent = new Intent(MainActivity.this, toClass);
        startActivity(intent);
    }
}
