package com.wanfuxiong.findfriends.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wanfuxiong.findfriends.R;
import com.wanfuxiong.findfriends.pojo.User;
import com.wanfuxiong.findfriends.util.MyUtils;
import com.wanfuxiong.findfriends.util.OkHttpUtils;
import com.wanfuxiong.findfriends.util.SqliteUtils;
import com.wanfuxiong.findfriends.util.UIUtils;
import com.wanfuxiong.findfriends.view.MyDialog;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class LoadActivity extends AppCompatActivity {

    private final static String TAG = "LoadActivity";
    private static final int LOAD_DISPLAY_TIME = 3000;// 持续时间

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 语言
        MyUtils.updateResources(LoadActivity.this);
        setContentView(R.layout.activity_load);

        final SharedPreferences sharedPreferences = this.getSharedPreferences("login", MODE_PRIVATE);
        final boolean autoLogin = sharedPreferences.getBoolean("autoLogin", false);
        if (autoLogin) {
            final String id = sharedPreferences.getString("id", "");
            final String password = sharedPreferences.getString("password", "");

            Map<String, Object> map = new HashMap<>();
            map.put("id", sharedPreferences.getString("id", ""));
            map.put("password", new String(Hex.encodeHex(DigestUtils.md5(password))));

            Call call = OkHttpUtils.getInstance().post("login", map);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoadActivity.this, R.string.error_internet, Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoadActivity.this, LoginActivity.class));
                            overridePendingTransition(0, 0);
                            finish();
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseString = response.body().string();
                    JSONObject resultJson = JSON.parseObject(responseString);
                    Integer code = resultJson.getInteger("code");
                    if (code == 1) {
                        User user = resultJson.getObject("user", User.class);

                        SQLiteDatabase sqLiteDatabase = openOrCreateDatabase("ff" + id + ".db", Context.MODE_PRIVATE, null);
                        SqliteUtils.init(sqLiteDatabase);
                        // 将user放进me数据表中
                        SqliteUtils.insertOrUpdateCurrentUserInfo(sqLiteDatabase, user);

                        Intent intent = MainActivity.newIntent(LoadActivity.this, Integer.parseInt(id), user.getUserUsername(), user.getUserInterest());
                        LoadActivity.this.startActivity(intent);
                        LoadActivity.this.finish();
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                startActivity(new Intent(LoadActivity.this, LoginActivity.class));
                                overridePendingTransition(0, 0);
                                finish();
                            }
                        });
                    }
                }
            });
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(LOAD_DISPLAY_TIME);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    startActivity(new Intent(LoadActivity.this, LoginActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                }
            }).start();
        }

    }

    // 关闭该Activity结束动画
    // @Override
    // public void finish() {
    //     super.finish();
    //     overridePendingTransition(0, 0);
    // }

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
    }
}
