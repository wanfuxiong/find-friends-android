package com.wanfuxiong.findfriends.util;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.wanfuxiong.findfriends.pojo.Moment;
import com.wanfuxiong.findfriends.pojo.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SqliteUtils {

    private final static String TAG = "SqlUtils";

    public static void init(SQLiteDatabase sqLiteDatabase) {
        // 主页表，MainActivity中显示与当前登录进来的用户兴趣相同的用户
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS `home` (" +
                "  `user_id` int(8) NOT NULL," +
                "  `user_username` varchar(16) NOT NULL DEFAULT '未命名'," +
                "  `user_signature` varchar(45)," +
                "  `user_sex` tinyint(4)," +
                "  `user_interest` varchar(50)," +
                "  `user_phone_number` varchar(11)," +
                "  `user_qq` varchar(10)," +
                "  `create_date` timestamp," +
                "  PRIMARY KEY (`user_id`)" +
                ")");
        // 消息表，HomeFragment中示最新消息
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS `message` (" +
                "  `user_id` int(8) NOT NULL," +
                "  `user_username` varchar(16) NOT NULL DEFAULT '未命名'," +
                "  `user_sex` tinyint(4)," +
                "  `message_data` varchar(45) NOT NULL DEFAULT ''," +
                "  `user_profile` mediumblob," +
                "  `date` timestamp NOT NULL DEFAULT (datetime(CURRENT_TIMESTAMP,'localtime'))," +
                "  `unread` tinyint(4) NOT NULL DEFAULT 0," +
                "  PRIMARY KEY (`user_id`)" +
                ")");
        // 消息记录表，ChatActivity中显示完整的消息记录
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS `chat_record` (" +
                "  `from_user_id` int(8) NOT NULL," +
                "  `to_user_id` int(8) NOT NULL," +
                "  `message_data` varchar(100) NOT NULL," +
                "  `date` timestamp NOT NULL DEFAULT (datetime(CURRENT_TIMESTAMP,'localtime'))" +
                ")");
        //自己的信息表，刷新home页面或进行其他操作时，应从数据库获取用户信息，而不是从activity或intent中获取。用户登录后，立刻更新该表信息。修改信息时，先更新服务端数据，然后更新该表信息以便用户的后续操作。
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS `me` (" +
                "  `user_id` int(8) NOT NULL," +
                "  `user_username` varchar(16) NOT NULL DEFAULT '未命名'," +
                "  `user_password` varchar(16) NOT NULL DEFAULT '1'," +
                "  `user_signature` varchar(45)," +
                "  `user_sex` tinyint(4)," +
                "  `user_interest` varchar(50)," +
                "  `user_phone_number` varchar(11)," +
                "  `user_qq` varchar(10)," +
                "  `user_profile` mediumblob," +
                "  `create_date` timestamp," +
                "  PRIMARY KEY (`user_id`)" +
                ")");
        // 用户信息表，用于存储访问过的用户信息
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS `user` (" +
                "  `user_id` int(8) NOT NULL," +
                "  `user_username` varchar(16) NOT NULL DEFAULT '未命名'," +
                "  `user_signature` varchar(45)," +
                "  `user_sex` tinyint(4)," +
                "  `user_interest` varchar(50)," +
                "  `user_phone_number` varchar(11)," +
                "  `user_qq` varchar(10)," +
                "  `user_profile` mediumblob," +
                "  `create_date` timestamp," +
                "  PRIMARY KEY (`user_id`)" +
                ")");
        // 用户信息表，用于存储访问过的用户信息
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS `moment` (" +
                "  `user_id` int(8) NOT NULL," +
                "  `moment_content` varchar(100)," +
                "  `moment_image` mediumblob," +
                "  `send_date` timestamp" +
                ")");
    }

    public static void updateHome(SQLiteDatabase sqLiteDatabase) {

    }

    // 更新用户信息
    public static void insertOrUpdateUserInfo(SQLiteDatabase sqLiteDatabase, User user) {
        Cursor cursor = sqLiteDatabase.query("user", null, null, null, null, null, null);
        cursor.moveToFirst();
        ContentValues cv = new ContentValues();
        cv.put("user_username", user.getUserUsername());
        cv.put("user_signature", user.getUserSignature());
        cv.put("user_sex", user.getUserSex());
        cv.put("user_interest", user.getUserInterest());
        cv.put("user_phone_number", user.getUserPhoneNumber());
        cv.put("user_qq", user.getUserQQ());
        cv.put("user_profile", user.getUserProfile());
        cv.put("create_date", user.getCreateDate().toString());

        boolean isUserExists = false;
        for (int i = 0; i < cursor.getCount(); i++) {
            if (cursor.getInt(0) == user.getUserID()) {
                sqLiteDatabase.update("user", cv, "user_id=?", new String[]{user.getUserID().toString()});
                isUserExists = true;
                break;
            }
            cursor.moveToNext();
        }
        if (!isUserExists) {
            cv.put("user_id", user.getUserID());
            sqLiteDatabase.insert("user", null, cv);
        }
    }

    // 获取用户信息
    public static User getUserInfo(SQLiteDatabase sqLiteDatabase, String userID) {
        Log.d(TAG, "正在获取指定id为" + userID + "的用户信息");
        // String[] columns = new String[]{
        //         "user_id",
        //         "user_username",
        //         "user_signature",
        //         "user_sex",
        //         "user_interest",
        //         "user_phone_number",
        //         "user_qq",
        //         "user_profile",
        // };
        // Cursor cursor = sqLiteDatabase.query("user", null, "user_id=?", new String[]{userID}, null, null, null);
        Cursor cursor = sqLiteDatabase.query("user", null, null, null, null, null, null);
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            if (cursor.getInt(0) == Integer.parseInt(userID)) {
                break;
            }
            cursor.moveToNext();
        }
        Log.d(TAG, "该用户在表中的位置：" + cursor.getPosition());
        return new User(
                cursor.getInt(0),
                cursor.getString(1),
                null,
                cursor.getString(2),
                cursor.getInt(3),
                cursor.getString(4),
                cursor.getString(5),
                cursor.getString(6),
                cursor.getBlob(7),
                null
        );
    }

    // 更新当前用户信息
    public static void insertOrUpdateCurrentUserInfo(SQLiteDatabase sqLiteDatabase, User currentUser) {
        ContentValues cv = new ContentValues();
        Cursor cursor = sqLiteDatabase.query("me", null, null, null, null, null, null);
        cursor.moveToFirst();
        cv.put("user_username", currentUser.getUserUsername());
        cv.put("user_password", currentUser.getUserPassword());
        cv.put("user_signature", currentUser.getUserSignature());
        cv.put("user_sex", currentUser.getUserSex());
        cv.put("user_interest", currentUser.getUserInterest());
        cv.put("user_phone_number", currentUser.getUserPhoneNumber());
        cv.put("user_qq", currentUser.getUserQQ());
        cv.put("user_profile", currentUser.getUserProfile());
        cv.put("create_date", currentUser.getCreateDate().toString());
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            sqLiteDatabase.update("me", cv, "user_id=?", new String[]{currentUser.getUserID().toString()});
        } else {
            cv.put("user_id", currentUser.getUserID());
            sqLiteDatabase.insert("me", null, cv);
        }
        cursor.close();
    }

    // 获取当前用户信息
    public static User getCurrentUserInfo(SQLiteDatabase sqLiteDatabase) {
        Cursor cursor = sqLiteDatabase.query("me", null, null, null, null, null, null);
        cursor.moveToFirst();
        return new User(
                cursor.getInt(0),
                cursor.getString(1),
                cursor.getString(2),
                cursor.getString(3),
                cursor.getInt(4),
                cursor.getString(5),
                cursor.getString(6),
                cursor.getString(7),
                cursor.getBlob(8),
                new Date(cursor.getString(9))
        );
    }

    public static void setMessageUnread(SQLiteDatabase sqLiteDatabase, int toUserID) {
        Cursor cursor = sqLiteDatabase.query("message", null, null, null, null, null, null);
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            if (cursor.getInt(0) == toUserID) {
                ContentValues cv = new ContentValues();
                cv.put("unread", cursor.getInt(6) + 1);
                sqLiteDatabase.update("message", cv, "user_id=?", new String[]{String.valueOf(toUserID)});
                break;
            }
            cursor.moveToNext();
        }
        cursor.close();
    }

    // 将消息设为已读
    public static void setMessageRead(SQLiteDatabase sqLiteDatabase, int toUserID) {
        Cursor cursor = sqLiteDatabase.query("message", null, null, null, null, null, null);
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            if (cursor.getInt(0) == toUserID) {
                ContentValues cv = new ContentValues();
                cv.put("unread", 0);
                sqLiteDatabase.update("message", cv, "user_id=?", new String[]{String.valueOf(toUserID)});
                break;
            }
            cursor.moveToNext();
        }
        cursor.close();
    }

    // 获取message列表，按时间排序
    public static List<User> getMessageList(SQLiteDatabase sqLiteDatabase) {
        List<User> users = new ArrayList<>();
        Cursor cursor = sqLiteDatabase.query("message", null, null, null, null, null, "date desc");
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                User user = new User(
                        cursor.getInt(0),
                        cursor.getString(1),
                        null,
                        null,
                        cursor.getInt(2),
                        null,
                        null,
                        null,
                        cursor.getBlob(4),
                        null
                );
                users.add(user);
            }
            cursor.close();
        }
        return users;
    }

    // 更新某个用户的moment
    public static void updateUserMoments(SQLiteDatabase sqLiteDatabase, int id, List<Moment> moments) {
        Log.d(TAG, "正在更新id为" + id + "用户的动态");
        sqLiteDatabase.delete("moment", "user_id=?", new String[]{String.valueOf(id)});
        for (int i = 0; i < moments.size(); i++) {
            ContentValues cv = new ContentValues();
            cv.put("user_id", moments.get(i).getUserID());
            cv.put("moment_content", moments.get(i).getMomentContent());
            cv.put("moment_image", moments.get(i).getMomentImage());
            cv.put("send_date", moments.get(i).getSendDate().toString());
            sqLiteDatabase.insert("moment", null, cv);
        }
    }

    // 插入一条当前用户的moment（当前用户发了一条新动态）
    public static void insertCurrentUserMoment(SQLiteDatabase sqLiteDatabase, Moment moment) {
        ContentValues cv = new ContentValues();
        cv.put("user_id", moment.getUserID());
        cv.put("moment_content", moment.getMomentContent());
        cv.put("moment_image", moment.getMomentImage());
        cv.put("send_date", moment.getSendDate().toString());
        sqLiteDatabase.insert("moment", null, cv);
    }

    // 获取某个用户的moment
    public static List<Moment> getMomentList(SQLiteDatabase sqLiteDatabase, int id) {
        List<Moment> moments = new ArrayList<>();
        Cursor cursor = sqLiteDatabase.query("moment", null, "user_id=?", new String[]{String.valueOf(id)}, null, null, "send_date desc");
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                Moment moment = new Moment(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getBlob(2),
                        new Date(cursor.getString(3))
                );
                moments.add(moment);
            }
            cursor.close();
        }
        return moments;
    }
}