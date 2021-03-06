package com.wanfuxiong.findfriends.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.wanfuxiong.findfriends.R;
import com.google.android.material.button.MaterialButton;
import com.wanfuxiong.findfriends.pojo.User;
import com.wanfuxiong.findfriends.util.OkHttpUtils;
import com.wanfuxiong.findfriends.util.SqliteUtils;
import com.wanfuxiong.findfriends.util.UIUtils;
import com.wanfuxiong.findfriends.view.MyDialog;
import com.wanfuxiong.findfriends.view.MyProgressDialog;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, View.OnKeyListener {

    private final static String TAG = "LoginActivity";

    private static long mExitTime = System.currentTimeMillis(); //??????????????????????????????????????????????????????????????????

    private ConstraintLayout mConstraintLayoutLogo;
    private MaterialButton mButtonLogin;                        // ????????????
    private EditText mEditTextID;                               // FF????????????
    private EditText mEditTextPassword;                         // ???????????????
    private TextView mTextViewGoToRegister;                     // "?????????"??????
    private ImageView mImageViewWipePassword;                   // "????????????"?????????
    private MaterialCheckBox mCheckBoxAutoLogin;                // "????????????"?????????

    private SharedPreferences sharedPreferences;

    private boolean autoLogin = false;                   // "????????????"???????????????

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Log.d(TAG, "onCreate");

        // UIUtils.setStatusBarColor(LoginActivity.this, ContextCompat.getColor(LoginActivity.this,R.color.colorAccent));
        UIUtils.setAndroidNativeLightStatusBar(LoginActivity.this, true);

        initView();
        // myRequetPermission();
        Animation animation = AnimationUtils.loadAnimation(LoginActivity.this, R.anim.login_logo);
        mConstraintLayoutLogo.startAnimation(animation);
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

        // ??????????????????????????? & ??????????????????????????????
        sharedPreferences = this.getSharedPreferences("login", MODE_PRIVATE);
        autoLogin = sharedPreferences.getBoolean("autoLogin", false);
        if (autoLogin) {
            mEditTextID.setText(sharedPreferences.getString("id", ""));
            mEditTextPassword.setText(sharedPreferences.getString("password", ""));
            mCheckBoxAutoLogin.setChecked(true);
        } else {
            mEditTextID.setText(sharedPreferences.getString("id", ""));
            mCheckBoxAutoLogin.setChecked(false);
        }
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

    private void initView() {

        Button button = findViewById(R.id.test);
        button.setVisibility(View.GONE);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setItems(new String[]{"1", "2"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();// ????????????show???????????????
                Window window = dialog.getWindow();
                WindowManager.LayoutParams lp = window.getAttributes();
                lp.width = 400;
                lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                window.setAttributes(lp);
                window.setBackgroundDrawableResource(R.drawable.background_dialog);
            }
        });

        // ????????????
        mConstraintLayoutLogo = findViewById(R.id.constraintlayout_logo);
        mButtonLogin = findViewById(R.id.button_login);
        mEditTextID = findViewById(R.id.edittext_id);
        mEditTextPassword = findViewById(R.id.edittext_password);
        mTextViewGoToRegister = findViewById(R.id.textview_gotoregister);
        mImageViewWipePassword = findViewById(R.id.imageview_wipe_password);
        mCheckBoxAutoLogin = findViewById(R.id.checkbox_auto_login);

        mImageViewWipePassword.setVisibility(View.GONE);

        // ??????????????????
        mButtonLogin.setOnClickListener(this);
        mTextViewGoToRegister.setOnClickListener(this);
        mImageViewWipePassword.setOnClickListener(this);
        mEditTextPassword.setOnKeyListener(this);
        mCheckBoxAutoLogin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                autoLogin = isChecked;
            }
        });

        // FF????????????????????????
        mEditTextID.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ??????EditText????????????
                if (s.toString().contains(" ")) {
                    String[] str = s.toString().split(" ");
                    StringBuilder sb = new StringBuilder();
                    for (String value : str) {
                        sb.append(value);
                    }
                    mEditTextID.setText(sb.toString());
                    mEditTextID.setSelection(start);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // ???????????????????????????
        mEditTextPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ??????EditText????????????
                if (s.toString().contains(" ")) {
                    String[] str = s.toString().split(" ");
                    StringBuilder sb = new StringBuilder();
                    for (String value : str) {
                        sb.append(value);
                    }
                    mEditTextPassword.setText(sb.toString());
                    mEditTextPassword.setSelection(start);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // ????????????"????????????"??????
                if (mEditTextPassword.getText().length() == 0) {
                    mImageViewWipePassword.setVisibility(View.GONE);
                } else {
                    mImageViewWipePassword.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    // ????????????
    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_login:         // ??????
                login();
                break;
            case R.id.imageview_wipe_password:     // ????????????
                mEditTextPassword.setText("");
                break;
            case R.id.textview_gotoregister:
                Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(registerIntent);
                break;
        }
    }

    // ????????????
    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        // ????????????
        if (v.getId() == R.id.edittext_password) {
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                login();
                return true;
            }
        }
        return false;
    }

    // ????????????????????????????????????????????????????????????
    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - mExitTime < 1000) {
            LoginActivity.this.finish();   //?????????????????????
        } else {
            Toast.makeText(LoginActivity.this, "????????????????????????", Toast.LENGTH_SHORT).show();
            mExitTime = System.currentTimeMillis();   //?????????????????????????????????
        }
    }

    // ????????????
    private void login() {
        mButtonLogin.setEnabled(false);
        final String id = mEditTextID.getText().toString().trim();//trim()???????????????????????????????????????
        final String password = mEditTextPassword.getText().toString().trim();
        if (TextUtils.isEmpty(id) && !TextUtils.isEmpty(password)) {
            new MyDialog.Builder(LoginActivity.this)
                    .setIcon(R.drawable.ic_dialog_smile)
                    // .setTitle("???????????????????")
                    .setMessage("FF????????????????????????")
                    .setPositiveButton(getString(R.string.text_ok), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    }).create().show();
            mButtonLogin.setEnabled(true);
        } else if (!TextUtils.isEmpty(id) && TextUtils.isEmpty(password)) {
            new MyDialog.Builder(LoginActivity.this)
                    .setIcon(R.drawable.ic_dialog_smile)
                    // .setTitle("???????????????????")
                    .setMessage("???????????????????????????")
                    .setPositiveButton(getString(R.string.text_ok), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    }).create().show();
            mButtonLogin.setEnabled(true);
        } else if (TextUtils.isEmpty(id) && TextUtils.isEmpty(password)) {
            new MyDialog.Builder(LoginActivity.this)
                    .setIcon(R.drawable.ic_dialog_smile)
                    // .setTitle("???????????????????")
                    .setMessage("FF?????????????????????????????????")
                    .setPositiveButton(getString(R.string.text_ok), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    }).create().show();
            mButtonLogin.setEnabled(true);
        } else {
            // final ProgressDialog progressDialog = ProgressDialog.show(this, null, "??????????????????", true, false, null);
            final MyProgressDialog progressDialog = MyProgressDialog.show(this, getString(R.string.text_loading_login), false, null);
            Map<String, Object> map = new HashMap<>();
            map.put("id", id);
            map.put("password", new String(Hex.encodeHex(DigestUtils.md5(password))));

            Call call = OkHttpUtils.getInstance().post("login", map);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoginActivity.this, R.string.error_internet, Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                            mButtonLogin.setEnabled(true);
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
                        // ???user??????me????????????
                        SqliteUtils.insertOrUpdateCurrentUserInfo(sqLiteDatabase, user);

                        // ??????"????????????"????????????boolean??????????????????
                        if (autoLogin) {
                            saveLoginData(id, password, autoLogin);
                        } else {
                            saveLoginData(id, "", autoLogin);
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                            }
                        });

                        Intent intent = MainActivity.newIntent(LoginActivity.this, Integer.parseInt(id), user.getUserUsername(), user.getUserInterest());
                        LoginActivity.this.startActivity(intent);
                        LoginActivity.this.finish();
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                                // Toast.makeText(getApplicationContext(), "????????????", Toast.LENGTH_SHORT).show();
                                new MyDialog.Builder(LoginActivity.this)
                                        .setIcon(R.drawable.ic_dialog_warn)
                                        // .setTitle("???????????????????")
                                        .setMessage("?????????????????????")
                                        .setPositiveButton(getString(R.string.text_ok), new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                            }
                                        }).create().show();
                                mButtonLogin.setEnabled(true);
                            }
                        });
                    }
                }
            });

        }
    }

    // ???????????????????????????FF????????????????????????????????????
    private void saveLoginData(String id, String password, boolean autoLogin) {
        // boolean flag = false;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("id", id);
        editor.putString("password", password);
        editor.putBoolean("autoLogin", autoLogin);
        editor.apply();
        // flag = editor.commit();
    }

    // private void myRequetPermission() {
    //     if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
    //
    //         ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
    //     } else {
    //         Toast.makeText(this, "????????????????????????!", Toast.LENGTH_SHORT).show();
    //     }
    // }

}