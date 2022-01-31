package com.wanfuxiong.findfriends.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wanfuxiong.findfriends.R;
import com.wanfuxiong.findfriends.pojo.User;
import com.wanfuxiong.findfriends.util.OkHttpUtils;
import com.wanfuxiong.findfriends.util.SqliteUtils;
import com.wanfuxiong.findfriends.util.UIUtils;
import com.wanfuxiong.findfriends.view.MyProgressDialog;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class SearchActivity extends AppCompatActivity {

    private static final String EXTRA_USER_ID = "user_id";
    private static final String EXTRA_USER_USERNAME = "user_username";

    private Toolbar mToolbar;
    private EditText mEditTextSearch;
    private Button mButtonSearch;
    private View mViewUser;
    private ImageView mImageViewProfile;
    private ImageView mImageViewWipeSearch;
    private TextView mTextViewUsername;
    private TextView mTextViewInterest;
    private TextView mTextViewNull;

    private int currentUserID;
    private String currentUserUsername;

    public static Intent newIntent(Context context, int id, String username) {
        Intent intent = new Intent(context, SearchActivity.class);
        intent.putExtra(EXTRA_USER_ID, id);
        intent.putExtra(EXTRA_USER_USERNAME, username);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        currentUserID = getIntent().getIntExtra(EXTRA_USER_ID, 0);
        currentUserUsername = getIntent().getStringExtra(EXTRA_USER_USERNAME);

        mViewUser = findViewById(R.id.view_search_user);
        mEditTextSearch = findViewById(R.id.edittext_search_search);
        mButtonSearch = findViewById(R.id.button_search_search);
        mImageViewProfile = findViewById(R.id.imageview_search_user_profile);
        mImageViewWipeSearch = findViewById(R.id.imageview_search_wipe);
        mTextViewUsername = findViewById(R.id.textview_search_username);
        mTextViewInterest = findViewById(R.id.textview_search_interest);
        mTextViewNull = findViewById(R.id.textview_searc_null_user);
        mToolbar = findViewById(R.id.toolbar_search);

        ViewGroup.LayoutParams layoutParams = mToolbar.getLayoutParams();
        Log.d("Toolbar原高：", String.valueOf(layoutParams.height));
        layoutParams.height += UIUtils.getStatusBarHeight(SearchActivity.this);
        Log.d("Toolbar现高：", String.valueOf(layoutParams.height));
        mToolbar.setLayoutParams(layoutParams);

        mImageViewWipeSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditTextSearch.setText("");
            }
        });
        mButtonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchUserByID(mEditTextSearch.getText().toString().trim(), String.valueOf(currentUserID));
            }
        });
        mEditTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().contains(" ")) {
                    String[] str = s.toString().split(" ");
                    StringBuilder sb = new StringBuilder();
                    for (String value : str) {
                        sb.append(value);
                    }
                    mEditTextSearch.setText(sb.toString());
                    mEditTextSearch.setSelection(start);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // 是否显示"清空搜索"按钮
                if (mEditTextSearch.getText().length() == 0) {
                    mImageViewWipeSearch.setVisibility(View.GONE);
                } else {
                    mImageViewWipeSearch.setVisibility(View.VISIBLE);
                }
            }
        });
        mEditTextSearch.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    // 先隐藏软键盘
                    // ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                    //         .hideSoftInputFromWindow(SearchActivity.this.getCurrentFocus()
                    //                 .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    String searchValue = mEditTextSearch.getText().toString().trim();
                    if (!TextUtils.isEmpty(searchValue)) {
                        searchUserByID(searchValue, String.valueOf(currentUserID));
                    }
                }
                return false;
            }
        });
    }

    private void searchUserByID(String searchID, String currentID) {
        final MyProgressDialog progressDialog = MyProgressDialog.show(this, getString(R.string.text_loading_search), false, null);
        Map<String, Object> map = new HashMap<>();
        map.put("search_id", searchID);
        map.put("current_id", currentID);
        Call call = OkHttpUtils.getInstance().post("searchUserByID", map);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SearchActivity.this, R.string.error_internet, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseString = response.body().string();
                JSONObject resultJson = JSON.parseObject(responseString);
                int code = resultJson.getInteger("code");
                if (code == 1) {
                    final User user = resultJson.getObject("user", User.class);
                    SQLiteDatabase sqLiteDatabase = openOrCreateDatabase("ff" + currentUserID + ".db", Context.MODE_PRIVATE, null);
                    SqliteUtils.insertOrUpdateUserInfo(sqLiteDatabase, user);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mViewUser.setVisibility(View.VISIBLE);
                            mTextViewNull.setVisibility(View.GONE);
                            mImageViewProfile.setVisibility(View.VISIBLE);
                            UIUtils.setProfile(user, mImageViewProfile);
                            mTextViewUsername.setText(user.getUserUsername());
                            mTextViewInterest.setText(user.getUserInterest());
                            progressDialog.dismiss();
                            mViewUser.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    SearchActivity.this.startActivity(MomentActivity.newIntent(SearchActivity.this, currentUserID, user.getUserID()));
                                }
                            });
                        }
                    });
                } else if (code == 0 || code == 2) {// 2说明在搜自己
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mViewUser.setVisibility(View.VISIBLE);
                            mTextViewNull.setVisibility(View.VISIBLE);
                            mImageViewProfile.setVisibility(View.GONE);
                            progressDialog.dismiss();
                        }
                    });
                }
            }
        });

    }
}