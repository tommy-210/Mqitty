package com.mqitty;

import android.content.Intent;
import android.net.Uri;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
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
import java.util.Map;
import static com.mqitty.utils.Utils.*;

public class MainActivity extends AppCompatActivity {

    ImageView send_btn, receive_btn, settings_btn, filter_btn;
    SearchView searchView;
    Spinner theme_mode_dropdown, default_panel_dropdown;
    EditText limit_time_msg;
    ViewGroup sendPanelContainer, receivePanelContainer, settingsPanelContainer;
    FrameLayout filter_popup;
    Button clear_panel_btn;
    DataBaseHelper dataBaseHelper;

    private final android.os.Handler settingsHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable settingsRunnable;
    private static boolean isCleanupDone = false;
    String default_panel;

    private final MqttManager.SubscriptionListener subscriptionListener = count -> {
        runOnUiThread(() -> {
            if (receivePanelContainer.getVisibility() == View.VISIBLE) {
                refreshReceivePanelData(null);
            }
        });
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        dataBaseHelper = DataBaseHelper.getInstance(this);
        default_panel = dataBaseHelper.getSettingByLabel(DataBaseHelper.SettingsDB.DEFAULT_PANEL);

        checkNotificationPermission();
        initComponents();
        componentListener();
        
        MqttManager.getInstance().addSubscriptionListener(subscriptionListener);

        if (savedInstanceState != null) {
            int panel = savedInstanceState.getInt(EXTRA_PANEL, -1);
            if (panel != -1) getIntent().putExtra(EXTRA_PANEL, panel);
        }

        handleIntent(getIntent(), default_panel);

        // Initial setup for the default included layout
        setupInnerPanelListeners();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent, default_panel);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        int currentPanel = PANEL_SEND;
        if (receivePanelContainer.getVisibility() == View.VISIBLE) currentPanel = PANEL_RECEIVE;
        else if (settingsPanelContainer.getVisibility() == View.VISIBLE) currentPanel = PANEL_SETTINGS;
        outState.putInt(EXTRA_PANEL, currentPanel);
    }

    private void handleIntent(Intent intent, String defaultPanel) {
        String dp = defaultPanel;
        if (dp == null) {
            dp = String.valueOf(PANEL_SEND);
        }
        int panel = intent.getIntExtra(EXTRA_PANEL, Integer.parseInt(dp));
        // Show panel
        switch (panel) {
            case PANEL_SEND:
                showPanel(sendPanelContainer);
                refreshSendPanelData(null);
                break;
            case PANEL_RECEIVE:
                showPanel(receivePanelContainer);
                refreshReceivePanelData(null);
                break;
            case PANEL_SETTINGS:
                showPanel(settingsPanelContainer);
                break;
        }
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    Toast.makeText(this, "Notification permission denied. You won't see background status.", Toast.LENGTH_SHORT).show();
                }
            });

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
//        navbar
        send_btn = findViewById(R.id.send_btn);
        receive_btn = findViewById(R.id.receive_btn);
        settings_btn = findViewById(R.id.settings_btn);
//        subheader
        searchView = findViewById(R.id.search_view);
        filter_popup = findViewById(R.id.filter_popup_section);
        clear_panel_btn = findViewById(R.id.clear_panel_btn);
        filter_btn = findViewById(R.id.filter_btn);
        //main container
        sendPanelContainer = findViewById(R.id.send_panel_container);
        receivePanelContainer = findViewById(R.id.receive_panel_container);
        settingsPanelContainer = findViewById(R.id.settings_panel_container);

        // Pre-inflate layouts into containers
        LayoutInflater.from(this).inflate(R.layout.activity_send, sendPanelContainer, true);
        LayoutInflater.from(this).inflate(R.layout.activity_receive, receivePanelContainer, true);
        LayoutInflater.from(this).inflate(R.layout.activity_settings, settingsPanelContainer, true);

        initSettings();
    }

    private void initSettings() {
        theme_mode_dropdown = findViewById(R.id.theme_mode_dropdown);
        default_panel_dropdown = findViewById(R.id.default_panel_dropdown);
        limit_time_msg = findViewById(R.id.limit_time_msg_input);
    }

    private void loadSettings() {
        Map<String, String> settings = dataBaseHelper.getAllSettings();
        if (settings.isEmpty()) return;

        String theme = settings.get(DataBaseHelper.SettingsDB.THEME);
        if (theme != null) {
            applyTheme(theme);
            switch (theme) {
                case SYSTEM_THEME:
                    theme_mode_dropdown.setSelection(0);
                    break;
                case LIGHT_THEME:
                    theme_mode_dropdown.setSelection(1);
                    break;
                case DARK_THEME:
                    theme_mode_dropdown.setSelection(2);
                    break;
            }
        }

        String panel = settings.get(DataBaseHelper.SettingsDB.DEFAULT_PANEL);
        if (panel != null) {
            default_panel_dropdown.setSelection(Integer.parseInt(panel));
        }

        String limit = settings.get(DataBaseHelper.SettingsDB.LIMIT_TIME_MSG);
        if (limit != null) {
            limit_time_msg.setText(limit);
        }
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
        loadSettings();
    }


    private void addListenerOnSettings() {
        String[] items_theme = {SYSTEM_THEME, LIGHT_THEME, DARK_THEME};
        String[] items_panel = {"Send", "Receiver", "Settings"};

//        theme dropdown popup
        ArrayAdapter<String> adapter_theme = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items_theme);
        theme_mode_dropdown.setAdapter(adapter_theme);
        theme_mode_dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedTheme = items_theme[position];

                // Update the current intent to keep the Settings panel active after recreation
                getIntent().putExtra(EXTRA_PANEL, PANEL_SETTINGS);

                dataBaseHelper.updateSetting(DataBaseHelper.SettingsDB.THEME, selectedTheme);
                applyTheme(selectedTheme);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

//        default panel dropdown popup
        ArrayAdapter<String> adapter_panel = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items_panel);
        default_panel_dropdown.setAdapter(adapter_panel);
        default_panel_dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                dataBaseHelper.updateSetting(DataBaseHelper.SettingsDB.DEFAULT_PANEL, String.valueOf(position));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

//        limit time to store messages in chat
        limit_time_msg.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (settingsRunnable != null) {
                    settingsHandler.removeCallbacks(settingsRunnable);
                }
                settingsRunnable = () -> {
                    dataBaseHelper.updateSetting(DataBaseHelper.SettingsDB.LIMIT_TIME_MSG, s.toString());
                };
                settingsHandler.postDelayed(settingsRunnable, 2000); // 1-second delay
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

//        button for project repository
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
                startActivity(changeActivity(MainActivity.this, CreateSendActivity.class));
            });
        }
        
        //add custom receiver
        View addReceiveBtn = findViewById(R.id.add_receive_btn);
        if (addReceiveBtn != null) {
            addReceiveBtn.setOnClickListener(v -> {
                startActivity(changeActivity(MainActivity.this, CreateReceiveActivity.class));
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
            int childCount = container.getChildCount();
            if (childCount > 0) {
                container.removeViews(0, childCount - 1);
            }
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

        if (!isCleanupDone) {
            String limitStr = dataBaseHelper.getSettingByLabel(DataBaseHelper.SettingsDB.LIMIT_TIME_MSG);
            long timeLimit = (limitStr != null) ? Long.parseLong(limitStr) : 7;
            for (ReceiverModel model : receiverModels) {
                dataBaseHelper.deleteMessageTooOldFromChat(model.getId(), timeLimit);
            }
            isCleanupDone = true;
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
            startActivity(changeActivity(MainActivity.this, SendModify.class, EXTRA_ELEMENT_ID, sendModel.getId()));
            return false;
        });
//        send message btn for mqtt
        ImageView send_msg_btn = view.findViewById(R.id.send_msg_btn);
        send_msg_btn.setOnClickListener(v -> {
            Mqtt mqtt = MqttManager.getInstance().getMqtt(MainActivity.this, sendModel.getBroker());
            mqtt.connect(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    MqttManager.getInstance().addToSentQueue(sendModel.getMessage());
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
            startActivity(changeActivity(MainActivity.this, ReceiveModify.class, EXTRA_ELEMENT_ID, receiverModel.getId()));
            return false;
        });

        Mqtt mqtt = MqttManager.getInstance().getMqtt(MainActivity.this, receiverModel.getBroker());

//        open chat with specific topic
        view.setOnClickListener(v ->  {
            startActivity(changeActivity(MainActivity.this, ChatActivity.class, EXTRA_ELEMENT_ID, receiverModel.getId()));
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
            MqttManager.getInstance().addPersistentSubscription(MainActivity.this, receiverModel.getBroker(), receiverModel.getTopic(), receiverModel.getId());
            mqtt.connect(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    mqtt.subscribe(receiverModel.getTopic(), 1);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MqttManager.getInstance().removeSubscriptionListener(subscriptionListener);
    }

    private void applyTheme(String theme) {
        switch (theme) {
            case LIGHT_THEME:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case DARK_THEME:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case SYSTEM_THEME:
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }
}
