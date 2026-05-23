package com.mqitty;

import android.content.Intent;
import android.net.Uri;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.mqitty.database.DataBaseHelper;
import com.mqitty.manager.ReceiveManager;
import com.mqitty.ui.ChatActivity;
import com.mqitty.ui.ReceiveModify;
import com.mqitty.ui.SendModify;
import com.mqitty.model.ReceiverModel;
import com.mqitty.model.SendModel;
import com.mqitty.ui.CreateReceiveActivity;
import com.mqitty.ui.CreateSendActivity;
import com.mqitty.manager.SendManager;
import com.mqitty.mqtt.Mqtt;
import com.mqitty.mqtt.MqttManager;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ImageView send_btn, receive_btn, settings_btn, filter_btn;
    SearchView searchView;
    EditText limit_time_mgs;
    Spinner theme_mode_dropdown;
    ViewGroup sendPanelContainer, receivePanelContainer, settingsPanelContainer;
    FrameLayout filter_popup;
    Button clear_panel_btn;
    DataBaseHelper dataBaseHelper;

    public static final String EXTRA_PANEL = "panel";
    public static final int PANEL_SEND = 0;
    public static final int PANEL_RECEIVE = 1;
    public static final int PANEL_SETTINGS = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        dataBaseHelper = DataBaseHelper.getInstance(this);

        int panel = getIntent().getIntExtra(EXTRA_PANEL, PANEL_SEND);

        initComponents();
        componentListener();
        
        // Show panel
        switch (panel) {
            case PANEL_RECEIVE:
                showPanel(receivePanelContainer);
                break;
            case PANEL_SETTINGS:
                showPanel(settingsPanelContainer);
                break;
            case PANEL_SEND:
            default:
                showPanel(sendPanelContainer);
                break;
        }

        // Initial setup for the default included layout
        setupInnerPanelListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sendPanelContainer.getVisibility() == View.VISIBLE) {
            refreshSendPanelData(null);
        } else if (receivePanelContainer.getVisibility() == View.VISIBLE) {
            refreshReceivePanelData(null);
        }
    }

    private void initComponents() {
        send_btn = findViewById(R.id.send_btn);
        receive_btn = findViewById(R.id.receive_btn);
        settings_btn = findViewById(R.id.settings_btn);
        searchView = findViewById(R.id.search_view);
        //main container
        sendPanelContainer = findViewById(R.id.send_panel_container);
        receivePanelContainer = findViewById(R.id.receive_panel_container);
        settingsPanelContainer = findViewById(R.id.settings_panel_container);
        filter_popup = findViewById(R.id.filter_popup_section);
        clear_panel_btn = findViewById(R.id.clear_panel_btn);
        filter_btn = findViewById(R.id.filter_btn);

        // Pre-inflate layouts into containers
        LayoutInflater.from(this).inflate(R.layout.activity_send, sendPanelContainer, true);
        LayoutInflater.from(this).inflate(R.layout.activity_receive, receivePanelContainer, true);
        LayoutInflater.from(this).inflate(R.layout.activity_settings, settingsPanelContainer, true);

        theme_mode_dropdown = findViewById(R.id.theme_mode_dropdown);
        limit_time_mgs = findViewById(R.id.limit_time_msg_input);

        refreshSendPanelData(null);
        refreshReceivePanelData(null);
    }

    private void componentListener() {
//        send panel button
        send_btn.setOnClickListener(v -> {
            showPanel(sendPanelContainer);
            refreshSendPanelData(null);
        });
//        receive panel button
        receive_btn.setOnClickListener(v -> {
            showPanel(receivePanelContainer);
            refreshReceivePanelData(null);
        });
//        settings panel button
        settings_btn.setOnClickListener(v -> {
            showPanel(settingsPanelContainer);
        });
//        search view summit filter query
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {return false;}

            @Override
            public boolean onQueryTextSubmit(String query) {
                Toast.makeText(MainActivity.this, "Filter: " + query, Toast.LENGTH_SHORT).show();
                if(sendPanelContainer.getVisibility() == View.VISIBLE){
                    refreshSendPanelData(query);
                }else {
                    refreshReceivePanelData(query);
                }
                return false;
            }
        });
//        close search view and reload all elemets
        searchView.setOnCloseListener(() -> {
            if(sendPanelContainer.getVisibility() == View.VISIBLE) {
                refreshSendPanelData(null);
            }else {
                refreshReceivePanelData(null);
            }
            return false;
        });
        filter_btn.setOnClickListener(v -> {
            if(filter_popup.getVisibility() == View.GONE) {
                filter_popup.setVisibility(View.VISIBLE);
            }else {
                filter_popup.setVisibility(View.GONE);
            }
        });
        clear_panel_btn.setOnClickListener(v -> {
            String panel = sendPanelContainer.getVisibility() == View.VISIBLE ? "Send" : "Receiver";
            showConfirmDeleteElementsDialog(panel);
            if(panel.equals("Send")) {
                refreshSendPanelData(null);
            }else {
                refreshReceivePanelData(null);
            }
        });

        addListenerOnSettings();
    }

    private void addListenerOnSettings() {
        String[] items = {"Light", "Dark"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        theme_mode_dropdown.setAdapter(adapter);
        theme_mode_dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String value = items[position];
                Toast.makeText(MainActivity.this, "Theme: " + value, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        Button githubBtn = findViewById(R.id.github_btn);
        if (githubBtn != null) {
            githubBtn.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/tommy-210/Mqitty"));
                startActivity(intent);
            });
        }
    }

    private void showConfirmDeleteElementsDialog(String panel) throws Resources.NotFoundException {
        new AlertDialog.Builder(this)
                .setTitle("Confirm deletion")
                .setMessage("Do you really want to delete all elements of " + panel + " panel?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                    Toast.makeText(MainActivity.this, "Yes", Toast.LENGTH_SHORT).show();
                    if(panel.equals("Send")) {
                        dataBaseHelper.deleteAllFromSend();
                    }else {
                        dataBaseHelper.deleteAllFromReceiver();
                    }
                })
                .setNegativeButton(android.R.string.no, null).show();
    }

    private void showPanel(ViewGroup panelToShow) {
        sendPanelContainer.setVisibility(View.GONE);
        receivePanelContainer.setVisibility(View.GONE);
        settingsPanelContainer.setVisibility(View.GONE);

        panelToShow.setVisibility(View.VISIBLE);

        if (panelToShow == sendPanelContainer) {
            send_btn.setBackgroundResource(R.drawable.border_round);
            receive_btn.setBackground(null);
            settings_btn.setBackground(null);
        }else if (panelToShow == receivePanelContainer) {
            receive_btn.setBackgroundResource(R.drawable.border_round);
            send_btn.setBackground(null);
            settings_btn.setBackground(null);
        }else if(panelToShow == settingsPanelContainer) {
            settings_btn.setBackgroundResource(R.drawable.border_round);
            send_btn.setBackground(null);
            receive_btn.setBackground(null);
        }
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

    private void refreshSendPanelData(String filterQuery) {
        List<SendModel> sendModels;
        if(filterQuery != null) {
            sendModels = dataBaseHelper.getFilteredSend(filterQuery);
        }else {
            sendModels = dataBaseHelper.getEveryoneSend();
        }
        View sendRoot = findViewById(R.id.activity_send_root);
        if (sendRoot instanceof ViewGroup) {
            ViewGroup container = (ViewGroup) sendRoot;
            container.removeViews(0, container.getChildCount() - 1);
            // The panel was just inflated, so we add all stored models
            for (SendModel model : sendModels) {
                // Add every element to container
                View view = SendManager.addSendModelToLayout(container, model);
                if (view != null) {
                    // Add click listener for each element, to open modify panel
                    addListenerOnSends(view, model);
                }
            }
        }
    }

    private void refreshReceivePanelData(String filterQuery) {
        List<ReceiverModel> receiverModels;
        if(filterQuery != null){
            receiverModels = dataBaseHelper.getFilteredReceive(filterQuery);
        }else {
            receiverModels = dataBaseHelper.getEveryoneReceiver();
        }
        View receiveRoot = findViewById(R.id.activity_receive_root);
        if (receiveRoot instanceof ViewGroup) {
            ViewGroup container = (ViewGroup) receiveRoot;
            int childCount = container.getChildCount();
            if (childCount > 0) {
                container.removeViews(0, childCount - 1);
            }
            // The panel was just inflated, so we add all stored models
            for (ReceiverModel model : receiverModels) {
                View view = ReceiveManager.addReceiveModelToLayout(container, model);
                if (view != null) {
                    addListenerOnReceivers(view, model);
                }
            }
        }
    }

    private void addListenerOnSends(View view, SendModel sendModel) {
        view.setOnLongClickListener(v -> {
            Toast.makeText(MainActivity.this, "Send: " + sendModel.getName(), Toast.LENGTH_SHORT).show();
            changeActivityWithExtra(SendModify.class, "id", sendModel.getId());
            return false;
        });
//        send message btn for mqtt
        ImageView send_msg_btn = view.findViewById(R.id.send_msg_btn);
        send_msg_btn.setOnClickListener(v -> {
            Mqtt mqtt = MqttManager.getInstance().getMqtt(MainActivity.this, sendModel.getBroker());
            mqtt.connect(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    mqtt.publish(sendModel.getTopic(), sendModel.getMessage());
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Send: " + sendModel.getMessage(), Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Connection failed", Toast.LENGTH_SHORT).show());
                }
            });
        });
    }

    private void addListenerOnReceivers(View view, ReceiverModel receiverModel) {
//        open modify panel
        view.setOnLongClickListener(v -> {
            changeActivityWithExtra(ReceiveModify.class, "id", receiverModel.getId());
            return false;
        });
        
        Mqtt mqtt = MqttManager.getInstance().getMqtt(MainActivity.this, receiverModel.getBroker());
        
//        open chat with specific topic
        view.setOnClickListener(v ->  {
            changeActivityWithExtra(ChatActivity.class, "id", receiverModel.getId());
        });

//        receive message btn for mqtt
        ImageView play_receive_msg_btn = view.findViewById(R.id.play_receive_msg_btn);
        ImageView stop_receive_msg_btn = view.findViewById(R.id.stop_receive_msg_btn);

        if (MqttManager.getInstance().isSubscribed(receiverModel.getBroker(), receiverModel.getTopic())) {
            play_receive_msg_btn.setVisibility(View.GONE);
            stop_receive_msg_btn.setVisibility(View.VISIBLE);
        } else {
            play_receive_msg_btn.setVisibility(View.VISIBLE);
            stop_receive_msg_btn.setVisibility(View.GONE);
        }

        play_receive_msg_btn.setOnClickListener(v -> {
            play_receive_msg_btn.setVisibility(View.GONE);
            stop_receive_msg_btn.setVisibility(View.VISIBLE);
            MqttManager.getInstance().addPersistentSubscription(receiverModel.getBroker(), receiverModel.getTopic(), receiverModel.getId());
            mqtt.connect(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    mqtt.subscribe(receiverModel.getTopic(), 1);
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Connection success", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    MqttManager.getInstance().removePersistentSubscription(receiverModel.getBroker(), receiverModel.getTopic());
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Connection failed", Toast.LENGTH_SHORT).show();
                        play_receive_msg_btn.setVisibility(View.VISIBLE);
                        stop_receive_msg_btn.setVisibility(View.GONE);
                    });
                }
            });
        });
        stop_receive_msg_btn.setOnClickListener(v -> {
            play_receive_msg_btn.setVisibility(View.VISIBLE);
            stop_receive_msg_btn.setVisibility(View.GONE);
            mqtt.unsubscribe(receiverModel.getTopic());
            MqttManager.getInstance().removePersistentSubscription(receiverModel.getBroker(), receiverModel.getTopic());
        });
    }

    private void changeActivity(Class toClass) {
        Intent intent = new Intent(MainActivity.this, toClass);
        startActivity(intent);
    }

    private void changeActivityWithExtra(Class toClass, String name, int content) {
        Intent intent = new Intent(MainActivity.this, toClass);
        intent.putExtra(name, content);
        startActivity(intent);
    }
}
