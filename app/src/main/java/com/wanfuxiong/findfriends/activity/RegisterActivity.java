package com.wanfuxiong.findfriends.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
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

public class RegisterActivity extends AppCompatActivity {

    private final static String TAG = "RegisterActivity";

    private RadioGroup mRadioGroupSex;
    private RadioButton mRadioButtonMale;
    private RadioButton mRadioButtonFemale;
    private EditText mEditTextUsername;
    private EditText mEditTextPassword;
    private EditText mEditTextPasswordAgain;
    private EditText mEditTextInterest;
    private MaterialButton mMaterialButtonNext;

    private static long mExitTime = System.currentTimeMillis();  //第一次点击返回按钮时候的系统时间，单位：毫秒

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        UIUtils.setAndroidNativeLightStatusBar(RegisterActivity.this, true);
        initView();
    }

    private void initView() {
        mRadioGroupSex = findViewById(R.id.radiogroup_register_sex);
        mRadioButtonMale = findViewById(R.id.radiobutton_register_male);
        mRadioButtonFemale = findViewById(R.id.radiobutton_register_female);
        mEditTextUsername = findViewById(R.id.edittext_register_username);
        mEditTextPassword = findViewById(R.id.edittext_register_password);
        mEditTextPasswordAgain = findViewById(R.id.edittext_register_password_again);
        mEditTextInterest = findViewById(R.id.edittext_register_interest);
        mMaterialButtonNext = findViewById(R.id.button_next);

        final int[] sex = new int[1];
        sex[0] = 2;//2在数据库中也表示未填写

        mRadioGroupSex.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (mRadioButtonMale.getId() == checkedId) {
                    sex[0] = 1;
                } else if (mRadioButtonFemale.getId() == checkedId) {
                    sex[0] = 0;
                }
            }
        });

        mEditTextUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 禁止EditText输入空格
                if (s.toString().contains(" ")) {
                    String[] str = s.toString().split(" ");
                    StringBuilder sb = new StringBuilder();
                    for (String value : str) {
                        sb.append(value);
                    }
                    mEditTextUsername.setText(sb.toString());
                    mEditTextUsername.setSelection(start);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mEditTextPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 禁止EditText输入空格
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

            }
        });

        mEditTextPasswordAgain.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 禁止EditText输入空格
                if (s.toString().contains(" ")) {
                    String[] str = s.toString().split(" ");
                    StringBuilder sb = new StringBuilder();
                    for (String value : str) {
                        sb.append(value);
                    }
                    mEditTextPasswordAgain.setText(sb.toString());
                    mEditTextPasswordAgain.setSelection(start);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mEditTextInterest.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 禁止EditText输入空格
                if (s.toString().contains(" ")) {
                    String[] str = s.toString().split(" ");
                    StringBuilder sb = new StringBuilder();
                    for (String value : str) {
                        sb.append(value);
                    }
                    mEditTextInterest.setText(sb.toString());
                    mEditTextInterest.setSelection(start);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mMaterialButtonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toast.makeText(getApplicationContext(), "性别："+sex[0], Toast.LENGTH_SHORT).show();

                final String username = mEditTextUsername.getText().toString().trim();
                final String password = mEditTextPassword.getText().toString().trim();
                String passwordAgain = mEditTextPasswordAgain.getText().toString().trim();
                final String interest = mEditTextInterest.getText().toString().trim();

                boolean allNull = sex[0] == 2
                        && TextUtils.isEmpty(username)
                        && TextUtils.isEmpty(password)
                        && TextUtils.isEmpty(passwordAgain)
                        && TextUtils.isEmpty(interest);

                if (allNull) {
                    new MyDialog.Builder(RegisterActivity.this)
                            .setIcon(R.drawable.ic_dialog_smile)
                            // .setTitle("ฅ՞•ﻌ•՞ฅ")
                            .setMessage("注册信息还没有填写哦～")
                            .setPositiveButton(getString(R.string.text_ok), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            }).create().show();
                } else if (sex[0] == 2) {
                    new MyDialog.Builder(RegisterActivity.this)
                            .setIcon(R.drawable.ic_dialog_smile)
                            // .setTitle("ฅ՞•ﻌ•՞ฅ")
                            .setMessage("性别还没有填写哦～")
                            .setPositiveButton(getString(R.string.text_ok), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            }).create().show();
                } else if (TextUtils.isEmpty(username)) {
                    new MyDialog.Builder(RegisterActivity.this)
                            .setIcon(R.drawable.ic_dialog_smile)
                            // .setTitle("ฅ՞•ﻌ•՞ฅ")
                            .setMessage("用户名还没有填写哦～")
                            .setPositiveButton(getString(R.string.text_ok), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            }).create().show();
                } else if (TextUtils.isEmpty(password)) {
                    new MyDialog.Builder(RegisterActivity.this)
                            .setIcon(R.drawable.ic_dialog_smile)
                            // .setTitle("ฅ՞•ﻌ•՞ฅ")
                            .setMessage("密码还没有填写哦～")
                            .setPositiveButton(getString(R.string.text_ok), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            }).create().show();
                } else if (TextUtils.isEmpty(passwordAgain)) {
                    new MyDialog.Builder(RegisterActivity.this)
                            .setIcon(R.drawable.ic_dialog_smile)
                            // .setTitle("ฅ՞•ﻌ•՞ฅ")
                            .setMessage("密码要再输一遍哦～")
                            .setPositiveButton(getString(R.string.text_ok), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            }).create().show();
                } else if (!password.equals(passwordAgain)) {
                    new MyDialog.Builder(RegisterActivity.this)
                            .setIcon(R.drawable.ic_dialog_smile)
                            // .setTitle("ฅ՞•ﻌ•՞ฅ")
                            .setMessage("两次密码输入不一样呢～")
                            .setPositiveButton(getString(R.string.text_ok), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            }).create().show();
                } else {
                    // final String userContent = "username=" + username +
                    //         "&password=" + new String(Hex.encodeHex(DigestUtils.md5(password))) +
                    //         "&sex=" + sex[0] +
                    //         "&interest=" + interest;

                    // final ProgressDialog progressDialog = ProgressDialog.show(RegisterActivity.this, null, "正在注册…", true, false, null);
                    final MyProgressDialog progressDialog = MyProgressDialog.show(RegisterActivity.this, getString(R.string.text_loading_register), false, null);
                    mMaterialButtonNext.setEnabled(false);
                    Map<String, Object> map = new HashMap<>();
                    map.put("username", username);
                    map.put("password", new String(Hex.encodeHex(DigestUtils.md5(password))));
                    map.put("sex", sex[0]);
                    map.put("interest", interest);
                    Log.d("要发送的数据 ===> ", map.toString());

                    Call call = OkHttpUtils.getInstance().post("register", map);
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast toast = Toast.makeText(RegisterActivity.this, R.string.error_internet, Toast.LENGTH_SHORT);
                                    mMaterialButtonNext.setEnabled(true);
                                    progressDialog.dismiss();
                                }
                            });
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.dismiss();
                                }
                            });
                            String responseString = response.body().string();
                            JSONObject resultJson = JSON.parseObject(responseString);
                            Integer code = resultJson.getInteger("code");
                            Integer id = resultJson.getInteger("id");
                            String desc = resultJson.getString("desc");
                            if (code == 0) {
                                Looper.prepare();
                                Toast.makeText(RegisterActivity.this, "操作数据库失败", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            } else if (code == 1) {
                                User user = resultJson.getObject("user", User.class);
                                SQLiteDatabase sqLiteDatabase = openOrCreateDatabase("ff" + id + ".db", Context.MODE_PRIVATE, null);
                                SqliteUtils.init(sqLiteDatabase);
                                // 将user放进me数据表中，以便别的activity或fragment获取
                                SqliteUtils.insertOrUpdateCurrentUserInfo(sqLiteDatabase, user);
                                startActivity(FFActivity.newIntent(RegisterActivity.this, user.getUserID()));
                                finish();
                            } else if (code == 2) {
                                Looper.prepare();
                                Toast.makeText(RegisterActivity.this, "无可用id，请联系管理员13072191318", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            } else {
                                Looper.prepare();
                                Toast.makeText(RegisterActivity.this, "未知错误", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            }
                        }
                    });
                    // String response = HttpUtils.doPostRequest("register", userContent);
                    // JSONObject resultJson = JSON.parseObject(response);
                    // int code = resultJson.getInteger("code");
                    //     }
                    // }).start();
                }
            }
        });
    }


    // 重写返回键功能
    @SuppressLint("ResourceAsColor")
    @Override
    public void onBackPressed() {
        new MyDialog.Builder(RegisterActivity.this)
                .setIcon(R.drawable.ic_dialog_beg)
                // .setTitle("˚‧º·(˚ ˃̣̣̥᷄⌓˂̣̣̥᷅ )‧º·˚")
                .setMessage("注册就快完成了qwq真的要退出嘛")
                .setPositiveButton("继续注册", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                })
                .setNegativeButton("我要走啦", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        RegisterActivity.this.finish();
                    }
                }).create().show();
    }

}
