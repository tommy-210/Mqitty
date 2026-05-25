package com.mqitty.utils;

import android.content.Context;
import android.content.Intent;

public class Utils {

    private static final String SPACE = " ";

//    name for extra element in intent to change activity
    public static final String EXTRA_ELEMENT_ID = "id";
    public static final String EXTRA_PANEL = "panel";
//    id to identify panel to switch from it
    public static final int PANEL_SEND = 0;
    public static final int PANEL_RECEIVE = 1;
    public static final int PANEL_SETTINGS = 2;
//    theme mode
    public static final String SYSTEM_THEME = "system";
    public static final String LIGHT_THEME = "Light";
    public static final String DARK_THEME = "Dark";

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

    public static boolean checkInputFormReceive(String name, String desc, String broker, String topic) {
//        check technical input
        if(broker.isBlank() || topic.isBlank()) {
            return !broker.contains(SPACE) && !topic.contains(SPACE);
        }
//        check estetics input
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
}
