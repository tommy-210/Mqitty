package com.mqitty.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;

import com.mqitty.model.MessageModel;
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

    class ChatsDB {
        final static String TABLE = "CHAT_TABLE_";
        final static String COLUMN_ID = "ID";
        final static String COLUMN_MSG = "CHAT_MESSAGE";
        final static String COLUMN_IS_SEND = "CHAT_IS_SEND";
        final static String COLUMN_TIMESTAMP = "CHAT_TIMESTAMP";
    }

    final static int DATABASE_VERSION = 1;
    private static DataBaseHelper instance;

    public static synchronized DataBaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DataBaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DataBaseHelper(@Nullable Context context) {
        super(context, "mqitty.db", null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createSendTable = "CREATE TABLE " + SendDB.TABLE + " (" +
                SendDB.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SendDB.COLUMN_NAME + " TEXT, " +
                SendDB.COLUMN_DESC + " TEXT, " +
                SendDB.COLUMN_BROKER + " TEXT, " +
                SendDB.COLUMN_TOPIC + " TEXT, " +
                SendDB.COLUMN_MSG + " TEXT)";

        String createReceiverTable = "CREATE TABLE " + ReceiverDB.TABLE + " (" +
                ReceiverDB.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ReceiverDB.COLUMN_NAME + " TEXT, " +
                ReceiverDB.COLUMN_DESC + " TEXT, " +
                ReceiverDB.COLUMN_BROKER + " TEXT, " +
                ReceiverDB.COLUMN_TOPIC + " TEXT)";

        db.execSQL(createSendTable);
        db.execSQL(createReceiverTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + SendDB.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + ReceiverDB.TABLE);
        onCreate(db);
    }

    private String getChatTable(int id) {
        return ChatsDB.TABLE + String.valueOf(id);
    }

    public void createChatTable(int id) {
        String createChatTable = "CREATE TABLE IF NOT EXISTS " + getChatTable(id) + " (" +
                ChatsDB.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ChatsDB.COLUMN_MSG + " TEXT, " +
                ChatsDB.COLUMN_IS_SEND + " BOOLEAN, " +
                ChatsDB.COLUMN_TIMESTAMP + " LONG)";

        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(createChatTable);
    }

    public void removeChaTable(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        String removeChatTable = "DROP TABLE IF EXISTS " + getChatTable(id);

        db.execSQL(removeChatTable);
    }

    public boolean addOneMessage(int id, MessageModel messageModel) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(ChatsDB.COLUMN_MSG, messageModel.getMessage());
        contentValues.put(ChatsDB.COLUMN_IS_SEND, messageModel.isSend());
        contentValues.put(ChatsDB.COLUMN_TIMESTAMP, messageModel.getTimestamp());

        long insert = db.insert(getChatTable(id), null, contentValues);
        return insert != -1;
    }

    public List<MessageModel> getEveryoneMessageFromChatById(int id) {
        List<MessageModel> returnList = new ArrayList<>();

        String query = "SELECT * FROM " + getChatTable(id);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if(cursor.moveToFirst()) {
            do {
                int ID = cursor.getInt(0);
                String MSG = cursor.getString(1);
                boolean IS_SEND = cursor.getInt(2) == 1;
                long TIMESTAMP = cursor.getLong(3);

                MessageModel messageModel = new MessageModel(ID, MSG, IS_SEND, TIMESTAMP);
                returnList.add(messageModel);
            }while(cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return returnList;
    }

    public void deleteAllMessageFromChat(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "DELETE FROM " + getChatTable(id);

        db.execSQL(query);
    }

    public long addOneSend(SendModel sendModel) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(SendDB.COLUMN_NAME, sendModel.getName());
        contentValues.put(SendDB.COLUMN_DESC, sendModel.getDescription());
        contentValues.put(SendDB.COLUMN_BROKER, sendModel.getBroker());
        contentValues.put(SendDB.COLUMN_TOPIC, sendModel.getTopic());
        contentValues.put(SendDB.COLUMN_MSG, sendModel.getMessage());

        return db.insert(SendDB.TABLE, null, contentValues);
    }

    public long addOneReceiver(ReceiverModel receiverModel) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(ReceiverDB.COLUMN_NAME, receiverModel.getName());
        contentValues.put(ReceiverDB.COLUMN_DESC, receiverModel.getDescription());
        contentValues.put(ReceiverDB.COLUMN_BROKER, receiverModel.getBroker());
        contentValues.put(ReceiverDB.COLUMN_TOPIC, receiverModel.getTopic());

        return db.insert(ReceiverDB.TABLE, null, contentValues);
    }

    public List<SendModel> getFilteredSend(String query) {
        List<SendModel> returnList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String queryString = "SELECT * FROM " + SendDB.TABLE + " WHERE " +
                SendDB.COLUMN_NAME + " LIKE ? OR " +
                SendDB.COLUMN_DESC + " LIKE ?";

        String[] searchArgs = new String[]{"%" + query + "%", "%" + query + "%"};
        Cursor cursor = db.rawQuery(queryString, searchArgs);

        if (cursor.moveToFirst()) {
            do {
                int ID = cursor.getInt(0);
                String NAME = cursor.getString(1);
                String DESC = cursor.getString(2);
                String BROKER = cursor.getString(3);
                String TOPIC = cursor.getString(4);
                String MSG = cursor.getString(5);

                SendModel sendModel = new SendModel(ID, NAME, DESC, BROKER, TOPIC, MSG);
                returnList.add(sendModel);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return returnList;
    }

    public List<ReceiverModel> getFilteredReceive(String query) {
        List<ReceiverModel> returnList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String queryString = "SELECT * FROM " + ReceiverDB.TABLE + " WHERE " +
                ReceiverDB.COLUMN_NAME + " LIKE ? OR " +
                ReceiverDB.COLUMN_DESC + " LIKE ?";

        String[] searchArgs = new String[]{"%" + query + "%", "%" + query + "%"};
        Cursor cursor = db.rawQuery(queryString, searchArgs);

        if (cursor.moveToFirst()) {
            do {
                int ID = cursor.getInt(0);
                String NAME = cursor.getString(1);
                String DESC = cursor.getString(2);
                String BROKER = cursor.getString(3);
                String TOPIC = cursor.getString(4);

                ReceiverModel receiverModel = new ReceiverModel(ID, NAME, DESC, BROKER, TOPIC);
                returnList.add(receiverModel);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return returnList;
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

    public SendModel getSendById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String queryString = "SELECT * FROM " + SendDB.TABLE + " WHERE " + SendDB.COLUMN_ID + " = " + id;
        Cursor cursor = db.rawQuery(queryString, null);
        SendModel sendModel = null;
        if (cursor.moveToFirst()) {
            String NAME = cursor.getString(1);
            String DESC = cursor.getString(2);
            String BROKER = cursor.getString(3);
            String TOPIC = cursor.getString(4);
            String MSG = cursor.getString(5);
            sendModel = new SendModel(cursor.getInt(0), NAME, DESC, BROKER, TOPIC, MSG);
        }
        cursor.close();
        db.close();
        return sendModel;
    }

    public ReceiverModel getReceiverById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String queryString = "SELECT * FROM " + ReceiverDB.TABLE + " WHERE " + ReceiverDB.COLUMN_ID + " = " + id;
        Cursor cursor = db.rawQuery(queryString, null);
        ReceiverModel receiverModel = null;
        if (cursor.moveToFirst()) {
            String NAME = cursor.getString(1);
            String DESC = cursor.getString(2);
            String BROKER = cursor.getString(3);
            String TOPIC = cursor.getString(4);
            receiverModel = new ReceiverModel(cursor.getInt(0), NAME, DESC, BROKER, TOPIC);
        }
        cursor.close();
        db.close();
        return receiverModel;
    }

    public boolean updateOneSend(SendModel sendModel) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(SendDB.COLUMN_NAME, sendModel.getName());
        contentValues.put(SendDB.COLUMN_DESC, sendModel.getDescription());
        contentValues.put(SendDB.COLUMN_BROKER, sendModel.getBroker());
        contentValues.put(SendDB.COLUMN_TOPIC, sendModel.getTopic());
        contentValues.put(SendDB.COLUMN_MSG, sendModel.getMessage());

        int result = db.update(SendDB.TABLE, contentValues, SendDB.COLUMN_ID + " = ?", new String[]{String.valueOf(sendModel.getId())});
        return result > 0;
    }

    public boolean updateOneReceiver(ReceiverModel receiverModel) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(ReceiverDB.COLUMN_NAME, receiverModel.getName());
        contentValues.put(ReceiverDB.COLUMN_DESC, receiverModel.getDescription());
        contentValues.put(ReceiverDB.COLUMN_BROKER, receiverModel.getBroker());
        contentValues.put(ReceiverDB.COLUMN_TOPIC, receiverModel.getTopic());

        int result = db.update(ReceiverDB.TABLE, contentValues, ReceiverDB.COLUMN_ID + " = ?", new String[]{String.valueOf(receiverModel.getId())});
        return result > 0;
    }

    public boolean deleteOneSend(SendModel sendModel) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(SendDB.TABLE, SendDB.COLUMN_ID + " = ?", new String[]{String.valueOf(sendModel.getId())});
        return result > 0;
    }

    public boolean deleteOneReceiver(ReceiverModel receiverModel) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(ReceiverDB.TABLE, ReceiverDB.COLUMN_ID + " = ?", new String[]{String.valueOf(receiverModel.getId())});
        return result > 0;
    }

    public void deleteAllFromSend() {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "DELETE FROM " + SendDB.TABLE;

        db.execSQL(query);
    }

    public void deleteAllFromReceiver() {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "DELETE FROM " + ReceiverDB.TABLE;

        db.execSQL(query);
    }
}
