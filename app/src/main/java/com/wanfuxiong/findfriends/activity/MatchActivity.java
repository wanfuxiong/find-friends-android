package com.wanfuxiong.findfriends.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.wanfuxiong.findfriends.R;
import com.wanfuxiong.findfriends.pojo.User;
import com.wanfuxiong.findfriends.util.OkHttpUtils;
import com.wanfuxiong.findfriends.util.SqliteUtils;
import com.wanfuxiong.findfriends.view.MyDialog;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import lombok.SneakyThrows;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MatchActivity extends AppCompatActivity {

    private static final String EXTRA_USER_ID = "user_id";
    private static final String EXTRA_USER_USERNAME = "user_username";
    private static final String EXTRA_USER_SEX = "user_sex";
    private static final String EXTRA_USER_INTEREST = "user_interest";

    public static Intent newIntent(Context context, int userID, String username, int userSex, String userInterest) {
        Intent intent = new Intent(context, MatchActivity.class);
        intent.putExtra(EXTRA_USER_ID, userID);
        intent.putExtra(EXTRA_USER_USERNAME, username);
        intent.putExtra(EXTRA_USER_SEX, userSex);
        intent.putExtra(EXTRA_USER_INTEREST, userInterest);
        return intent;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match);

        // ImageView imageView = findViewById(R.id.imageview_match);
        // Glide.with(MatchActivity.this).load(R.drawable.gif_matching).asGif().diskCacheStrategy(DiskCacheStrategy.SOURCE).into(imageView);

        WebView webView = findViewById(R.id.webview_match);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.loadUrl("file:///android_asset/atom.html");//加载asset文件夹下html

        final int id = getIntent().getIntExtra(EXTRA_USER_ID, 0);
        final String username = getIntent().getStringExtra(EXTRA_USER_USERNAME);
        final int sex = getIntent().getIntExtra(EXTRA_USER_SEX, 0);
        final String interest = getIntent().getStringExtra(EXTRA_USER_INTEREST);

        new Thread(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                Thread.sleep(5000);
                Map<String, Object> map = new HashMap<>();
                map.put("id", id);
                map.put("sex", sex);
                map.put("interest", interest);

                Call call = OkHttpUtils.getInstance().post("getRandomUser", map);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MatchActivity.this, R.string.error_internet, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String responseString = response.body().string();
                        JSONObject resultJson = JSON.parseObject(responseString);
                        User user = resultJson.getObject("user", User.class);
                        if (user != null) {
                            SqliteUtils.insertOrUpdateUserInfo(openOrCreateDatabase("ff" + id + ".db", Context.MODE_PRIVATE, null), user);
                            startActivity(MomentActivity.newIntent(MatchActivity.this, id, user.getUserID()));
                            finish();
                        } else {
                            new MyDialog.Builder(MatchActivity.this)
                                    .setIcon(R.drawable.ic_dialog_ask)
                                    .setMessage("暂时没有找到特别匹配的人")
                                    .setPositiveButton(getString(R.string.text_ok), new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            finish();
                                        }
                                    }).create().show();
                        }
                    }
                });
            }
        }).start();

    }
}