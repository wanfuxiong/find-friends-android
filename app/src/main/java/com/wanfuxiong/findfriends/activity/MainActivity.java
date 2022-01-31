package com.wanfuxiong.findfriends.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wanfuxiong.findfriends.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.wanfuxiong.findfriends.pojo.Message;
import com.wanfuxiong.findfriends.pojo.User;
import com.wanfuxiong.findfriends.util.MyUtils;
import com.wanfuxiong.findfriends.util.MyWebSocket;
import com.wanfuxiong.findfriends.util.OkHttpUtils;
import com.wanfuxiong.findfriends.util.SqliteUtils;
import com.wanfuxiong.findfriends.util.UIUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    public static MainActivity instance = null;

    private static final String TAG = "MainActivity";
    private static final String EXTRA_CURRENT_USER_ID = "current_user_id";
    private static final String EXTRA_CURRENT_USER_USERNAME = "current_user_username";
    private static final String EXTRA_CURRENT_USER_INTEREST = "current_user_interest";

    private static final String URL = MyUtils.WEBSOCKET + "://" + MyUtils.SERVER + ":" + MyUtils.PORT + "/websocket/";

    private int currentUserID;

    private SQLiteDatabase mSQLiteDatabase;

    public static MyWebSocket myWebSocketClient;

    public static Intent newIntent(Context context, int id, String username, String interest) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(EXTRA_CURRENT_USER_ID, id);
        intent.putExtra(EXTRA_CURRENT_USER_USERNAME, username);
        intent.putExtra(EXTRA_CURRENT_USER_INTEREST, interest);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate");

        // UIUtils.setStatusBarColor(MainActivity.this, ContextCompat.getColor(MainActivity.this,R.color.colorMyGrayLightest));
        // UIUtils.setAndroidNativeLightStatusBar(MainActivity.this,true);

        // UIUtils.setStatusBarColor(this,R.color.colorTransparent);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(R.id.navigation_home, R.id.navigation_message, R.id.navigation_me).build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        // NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        UIUtils.disableShiftMode(navView);

        instance = this;

        currentUserID = getIntent().getIntExtra(EXTRA_CURRENT_USER_ID, 0);

        // 创建或打开数据库
        mSQLiteDatabase = openOrCreateDatabase("ff" + currentUserID + ".db", Context.MODE_PRIVATE, null);

        // 注册EventBus事件
        Log.d(TAG,"注册EventBus事件");
        EventBus.getDefault().register(this);
        connectServer();


    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        EventBus.getDefault().unregister(this);
        Log.d(TAG, "正在断开webSocket连接");
        try {
            myWebSocketClient.closeBlocking();
            Log.d(TAG, "已断开webSocket连接");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void connectServer() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    myWebSocketClient = new MyWebSocket(URL + currentUserID);
                    if (myWebSocketClient.connectBlocking()) {
                        Log.i(TAG, "连接服务器成功");
                    } else {
                        Log.i(TAG, "连接服务器失败");
                    }
                } catch (InterruptedException | URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        };
        runnable.run();
    }

    // EventBus接收事件
    @SuppressLint("SimpleDateFormat")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateMsgView(final Message message) {
        Log.d(TAG, "EventBus");

        // 不是系统发的消息才需要操作数据库，如果是系统发的消息，就交给homefragment做
        if (message.getFromUserID()!=0){
            ContentValues cv = new ContentValues();
            cv.put("from_user_id", message.getFromUserID());
            cv.put("to_user_id", message.getToUserID());
            cv.put("message_data", message.getMessageData());
            mSQLiteDatabase.insert("chat_record", null, cv);
            Log.d(TAG, "EventBus 已在chat_record表插入了最新的消息");


            boolean isToUserExists = false;
            Cursor cursor = mSQLiteDatabase.query("message", null, null, null, null, null, null);
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                if (message.getFromUserID() == cursor.getInt(0) && currentUserID != message.getFromUserID()) {
                    Log.d(TAG, "EventBus 在message表中找到了该用户，消息是别人发给自己的");
                    isToUserExists = true;
                    ContentValues cv2 = new ContentValues();
                    cv2.put("message_data", message.getMessageData());
                    // cv2.put("date", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                    cv2.put("date", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(message.getDate()));
                    mSQLiteDatabase.update("message", cv2, "user_id=?", new String[]{message.getFromUserID().toString()});
                    // 未读消息+1
                    SqliteUtils.setMessageUnread(mSQLiteDatabase, message.getFromUserID());
                    break;
                } else if (message.getToUserID() == cursor.getInt(0) && currentUserID == message.getFromUserID()) {
                    Log.d(TAG, "EventBus 在message表中找到了该用户，消息是自己发给别人的");
                    isToUserExists = true;
                    ContentValues cv2 = new ContentValues();
                    cv2.put("message_data", message.getMessageData());
                    cv2.put("date", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(message.getDate()));
                    mSQLiteDatabase.update("message", cv2, "user_id=?", new String[]{message.getToUserID().toString()});
                    break;
                }
                cursor.moveToNext();
            }
            // 如果是一个message表中不存在的用户，并且该用户不是系统，那就获取他的个人信息，加进message表中后，刷新消息页面
            if (!isToUserExists) {
                Log.d(TAG, "EventBus 收到一条陌生人的消息了！！！");

                Map<String, Object> map = new HashMap<>();
                map.put("id", message.getFromUserID());// 获取发件人（fromuserid）的信息
                Call call = OkHttpUtils.getInstance().post("getUserInfo", map);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Looper.prepare();
                        Toast.makeText(MainActivity.this, R.string.error_internet, Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String responseString = response.body().string();
                        JSONObject resultJson = JSON.parseObject(responseString);
                        User user = resultJson.getObject("user", User.class);
                        SqliteUtils.insertOrUpdateUserInfo(mSQLiteDatabase, user);
                        ContentValues cv = new ContentValues();
                        cv.put("user_id", user.getUserID());
                        cv.put("user_username", user.getUserUsername());
                        cv.put("user_sex", user.getUserSex());
                        cv.put("message_data", message.getMessageData());
                        cv.put("user_profile", user.getUserProfile());
                        cv.put("date", MyUtils.convertDateToString(message.getDate()));
                        cv.put("unread", 1);
                        mSQLiteDatabase.insert("message", null, cv);
                        Log.d(TAG, "EventBus 新用户的新消息已经写入数据库");
                    }
                });
            }
        }

        Log.d(TAG, "EventBus 已完成所有sqlite操作");
    }

    // 重写返回键功能
    @Override
    public void onBackPressed() {
        // // 按两次返回键退出程序
        // if (System.currentTimeMillis() - mExitTime < 1000) {
        //     Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        //     startActivity(loginIntent);
        //     // 方法一 关闭本活动页面
        //     MainActivity.this.finish();
        //
        //     // 方法二 关闭进程
        //     // ActivityManager am = (ActivityManager)getSystemService (Context.ACTIVITY_SERVICE);
        //     // am.killBackgroundProcesses(getPackageName());
        //
        //     // 方法三 关闭进程
        //     // android.os.Process.killProcess(android.os.Process.myPid());
        // } else {
        //     Toast.makeText(getApplicationContext(), "再按一次注销登录", Toast.LENGTH_SHORT).show();
        //     mExitTime = System.currentTimeMillis();   //这里赋值最关键，别忘记
        // }
        moveTaskToBack(true);
    }

}
