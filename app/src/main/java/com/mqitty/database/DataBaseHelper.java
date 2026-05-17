package com.mqitty.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;
import com.mqitty.model.ReceiverModel;
import com.mqitty.model.SendModel;
import java.util.ArrayList;
import java.util.List;

public class DataBaseHelper extends SQLiteOpenHelper {

    class SendDB {
        final static String TABLE = "SEND_TABLE";
        final static String COLUMN_ID = "ID";
        final static String COLUMN_NAME = "SEND_NAME";
        final static String COLUMN_DESC = "SEND_DESC";
        final static String COLUMN_BROKER = "SEND_BROKER";
        final static String COLUMN_TOPIC = "SEND_TOPIC";
        final static String COLUMN_MSG = "SEND_MSG";
    }

    class ReceiverDB {
        final static String TABLE = "RECEIVER_TABLE";
        final static String COLUMN_ID = "ID";
        final static String COLUMN_NAME = "RECEIVER_NAME";
        final static String COLUMN_DESC = "RECEIVER_DESC";
        final static String COLUMN_BROKER = "RECEIVER_BROKER";
        final static String COLUMN_TOPIC = "RECEIVER_TOPIC";
    }

    private static DataBaseHelper instance;

    public static synchronized DataBaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DataBaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DataBaseHelper(@Nullable Context context) {
        super(context, "mqitty.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createSendTable = "CREATE TABLE " + SendDB.TABLE + " (" + SendDB.COLUMN_ID + " INTEGER PRIMARY KEY, " +
                SendDB.COLUMN_NAME + " TEXT, " + SendDB.COLUMN_DESC + " TEXT, " + SendDB.COLUMN_BROKER + " TEXT, " +
                SendDB.COLUMN_TOPIC + " TEXT, " + SendDB.COLUMN_MSG + " TEXT)";

        String createReceiverTable = "CREATE TABLE " + ReceiverDB.TABLE + " (" + ReceiverDB.COLUMN_ID + " INTEGER PRIMARY KEY, " +
                ReceiverDB.COLUMN_NAME + " TEXT, " + ReceiverDB.COLUMN_DESC + " TEXT, " + ReceiverDB.COLUMN_BROKER + " TEXT, " +
                ReceiverDB.COLUMN_TOPIC + " TEXT)";

        db.execSQL(createSendTable);
        db.execSQL(createReceiverTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public boolean addOneSend(SendModel sendModel) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(SendDB.COLUMN_NAME, sendModel.getName());
        contentValues.put(SendDB.COLUMN_DESC, sendModel.getDescription());
        contentValues.put(SendDB.COLUMN_BROKER, sendModel.getBroker());
        contentValues.put(SendDB.COLUMN_TOPIC, sendModel.getTopic());
        contentValues.put(SendDB.COLUMN_MSG, sendModel.getMessage());

        long insert = db.insert(SendDB.TABLE, null, contentValues);
        return insert != -1;
    }

    public boolean addOneReceiver(ReceiverModel receiverModel) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(ReceiverDB.COLUMN_NAME, receiverModel.getName());
        contentValues.put(ReceiverDB.COLUMN_DESC, receiverModel.getDescription());
        contentValues.put(ReceiverDB.COLUMN_BROKER, receiverModel.getBroker());
        contentValues.put(ReceiverDB.COLUMN_TOPIC, receiverModel.getTopic());

        long insert = db.insert(SendDB.TABLE, null, contentValues);
        return insert != -1;
    }

    public List<SendModel> getEveryoneSend() {
        List<SendModel> returnList = new ArrayList<>();

        String queryString = "SELECT * FROM " + SendDB.TABLE;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(queryString, null);

        if(cursor.moveToFirst()) {
            do {
                int ID = cursor.getInt(0);
                String NAME = cursor.getString(1);
                String DESC = cursor.getString(2);
                String BROKER = cursor.getString(3);
                String TOPIC = cursor.getString(4);
                String MSG = cursor.getString(5);

                SendModel sendModel = new SendModel(ID, NAME, DESC, BROKER, TOPIC, MSG);
                returnList.add(sendModel);
            }while(cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return returnList;
    }

    public List<ReceiverModel> getEveryoneReceiver() {
        List<ReceiverModel> returnList = new ArrayList<>();

        String queryString = "SELECT * FROM " + ReceiverDB.TABLE;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(queryString, null);

        if(cursor.moveToFirst()) {
            do {
                int ID = cursor.getInt(0);
                String NAME = cursor.getString(1);
                String DESC = cursor.getString(2);
                String BROKER = cursor.getString(3);
                String TOPIC = cursor.getString(4);

                ReceiverModel receiverModel = new ReceiverModel(ID, NAME, DESC, BROKER, TOPIC);
                returnList.add(receiverModel);
            }while(cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return returnList;
    }

    public boolean deleteOneSend(SendModel sendModel) {
        SQLiteDatabase db = this.getWritableDatabase();

        String queryString = "DELETE FROM " + SendDB.TABLE + " WHERE " + SendDB.COLUMN_ID + " = " + sendModel.getId();

        Cursor cursor = db.rawQuery(queryString, null);
        return cursor.moveToFirst();
    }

    public boolean deleteOneReceiver(ReceiverModel receiverModel) {
        SQLiteDatabase db = this.getWritableDatabase();

        String queryString = "DELETE FROM " + ReceiverDB.TABLE + " WHERE " + ReceiverDB.COLUMN_ID + " = " + receiverModel.getId();

        Cursor cursor = db.rawQuery(queryString, null);
        return cursor.moveToFirst();
    }
}
