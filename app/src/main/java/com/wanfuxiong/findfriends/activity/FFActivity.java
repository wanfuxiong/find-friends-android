package com.wanfuxiong.findfriends.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.wanfuxiong.findfriends.R;
import com.wanfuxiong.findfriends.pojo.User;
import com.wanfuxiong.findfriends.util.SqliteUtils;

public class FFActivity extends AppCompatActivity {

    private static final String EXTRA_USER_ID = "user_id";

    private TextView mTextViewFF;
    private Button mButtonNext;

    public static Intent newIntent(Context context, int id) {
        Intent intent = new Intent(context, FFActivity.class);
        intent.putExtra(EXTRA_USER_ID, id);
        return intent;
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ff);

        final int id = getIntent().getIntExtra(EXTRA_USER_ID, 0);
        SQLiteDatabase sqLiteDatabase = openOrCreateDatabase("ff" + id + ".db", Context.MODE_PRIVATE, null);
        SqliteUtils.init(sqLiteDatabase);
        final User user = SqliteUtils.getCurrentUserInfo(sqLiteDatabase);

        mTextViewFF = findViewById(R.id.textview_ff_ff);
        mButtonNext = findViewById(R.id.button_ff_next);

        mTextViewFF.setText(String.valueOf(id));
        mButtonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(MainActivity.newIntent(FFActivity.this, id, user.getUserInterest(), user.getUserInterest()));
            }
        });
    }
}