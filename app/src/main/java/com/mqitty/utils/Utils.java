package com.mqitty.utils;

import android.content.Context;
import android.content.Intent;
import com.mqitty.model.ReceiverModel;
import com.mqitty.model.SendModel;
import java.util.List;

public class Utils {
//    app information for updates
    public static final String GITHUB_USER = "tommy-210";
    public static final String GITHUB_REPOSITORY = "Mqitty";

    private static final String SPACE = " ";

//    name for extra element in intent to change activity
    public static final String EXTRA_ELEMENT_ID = "id";
    public static final String EXTRA_PANEL = "panel";
//    id to identify panel to switch from it
    public static final int PANEL_SEND = 0;
    public static final int PANEL_RECEIVE = 1;
    public static final int PANEL_SETTINGS = 2;
//    label for panel
    public static final String VALUE_SEND = "Send";
    public static final String VALUE_RECEIVER = "Receiver";
    public static final String VALUE_SETTINGS = "Settings";
//    theme mode
    public static final String SYSTEM_THEME = "system";
    public static final String LIGHT_THEME = "Light";
    public static final String DARK_THEME = "Dark";
//    notification
    public static final String NOTIFICATION_ENABLE = "true";
//    filter mode
    public static final int FILTER_NAME = 0;
    public static final int FILTER_BROKER = 1;
    public static final int FILTER_CUSTOM = 2;
//    notification type
    public static final int NO_NOTIFICATION = 1;
    public static final int ALL_NOTIFICATION = 2;
    public static final int CUSTOM_NOTIFICATION = 3;

//    check if input of from is correct
    public static boolean checkInputFormSend(String name, String desc, String broker, String topic, String msg) {
//        check technical input
        if(broker.isBlank() || topic.isBlank())
            return false;
        if(broker.contains(SPACE) || topic.contains(SPACE))
            return false;
//        check estetics input
        return !name.isBlank() && !desc.isBlank() && !msg.isBlank();
    }

    public static boolean checkInputFormReceive(String name, String desc, String broker, String topic, int notificationType, String keywordCustomNotification) {
//        check technical input
        if(broker.isBlank() || topic.isBlank()) {
            return !broker.contains(SPACE) && !topic.contains(SPACE);
        }
//        check notification section
        if(notificationType == -1) {
            return false;
        }else if(notificationType == CUSTOM_NOTIFICATION && keywordCustomNotification.isBlank()) {
            return false;
        }
//        check aesthetics input
        return !name.isBlank() && !desc.isBlank();
    }

//    change activity with extra components
    public static Intent changeActivity(Context fromContext, Class toClass, String extraName, int extraValue) {
        Intent intent = new Intent(fromContext, toClass);
        intent.putExtra(extraName, extraValue);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return intent;
    }
    public static Intent changeActivity(Context fromContext, Class toClass) {
        return new Intent(fromContext, toClass);
    }

//    sort a list of elements by alphabetic
    public static void sortSendList(List<SendModel> list, int mode) {
        if(mode == FILTER_CUSTOM) return;

        if(mode == FILTER_NAME) {
            list.sort((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
        }else if(mode == FILTER_BROKER) {
            list.sort((o1, o2) -> o1.getBroker().compareToIgnoreCase(o2.getBroker()));
        }
    }
    public static void sortReceiverList(List<ReceiverModel> list, int mode) {
        if(mode == FILTER_CUSTOM) return;

        if(mode == FILTER_NAME) {
            list.sort((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
        }else if(mode == FILTER_BROKER) {
            list.sort((o1, o2) -> o1.getBroker().compareToIgnoreCase(o2.getBroker()));
        }
    }
}
