package com.wanfuxiong.findfriends.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.Toolbar;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.android.material.button.MaterialButton;
import com.wanfuxiong.findfriends.R;
import com.wanfuxiong.findfriends.util.MyUtils;
import com.wanfuxiong.findfriends.util.OkHttpUtils;
import com.wanfuxiong.findfriends.util.UIUtils;
import com.wanfuxiong.findfriends.view.ChangePasswordDialog;
import com.wanfuxiong.findfriends.view.MyDialog;
import com.wanfuxiong.findfriends.view.MyProgressDialog;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import lombok.SneakyThrows;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String EXTRA_USER_ID = "user_id";

    private Toolbar mToolbar;
    private View mViewChangePassword;
    private View mViewWipeChartRecord;
    private View mViewChangeLanguage;
    private View mViewUpdate;
    private View mViewAbout;
    private View mViewLogout;
    private View mViewCloseAccount;

    private int currentUserID;
    private SQLiteDatabase mSQLiteDatabase;

    public static Intent newIntent(Context context, int id) {
        Intent intent = new Intent(context, SettingsActivity.class);
        intent.putExtra(EXTRA_USER_ID, id);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // UIUtils.setAndroidNativeLightStatusBar(SettingsActivity.this, true);
        currentUserID = getIntent().getIntExtra(EXTRA_USER_ID, 0);

        mViewChangePassword = findViewById(R.id.view_settings_change_password);
        mViewWipeChartRecord = findViewById(R.id.view_settings_wipe_chat_record);
        mViewChangeLanguage = findViewById(R.id.view_settings_change_language);
        mViewUpdate = findViewById(R.id.view_settings_update);
        mViewAbout = findViewById(R.id.view_settings_about);
        mViewLogout = findViewById(R.id.view_settings_logout);
        mViewCloseAccount = findViewById(R.id.view_settings_close_account);
        mToolbar = findViewById(R.id.toolbar_settings);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mViewChangePassword.setOnClickListener(this);
        mViewWipeChartRecord.setOnClickListener(this);
        mViewChangeLanguage.setOnClickListener(this);
        mViewUpdate.setOnClickListener(this);
        mViewAbout.setOnClickListener(this);
        mViewLogout.setOnClickListener(this);
        mViewCloseAccount.setOnClickListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.view_settings_change_password:
                changePassword();
                break;
            case R.id.view_settings_wipe_chat_record:
                wipeChatRecord();
                break;
            case R.id.view_settings_change_language:
                changeLanguage();
                break;
            case R.id.view_settings_update:
                final MyProgressDialog progressDialog = MyProgressDialog.show(SettingsActivity.this, getString(R.string.text_loading_update), false, null);
                new Thread(new Runnable() {
                    @SneakyThrows
                    @Override
                    public void run() {
                        Thread.sleep(3000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                                new MyDialog.Builder(SettingsActivity.this)
                                        .setIcon(R.drawable.ic_dialog_smile)
                                        .setMessage("当前已是最新版本！")
                                        .setPositiveButton(getString(R.string.text_ok), new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                            }
                                        }).create().show();
                            }
                        });
                    }
                }).start();
                break;
            case R.id.view_settings_about:
                startActivity(new Intent(SettingsActivity.this, AboutActivity.class));
                break;
            case R.id.view_settings_logout:
                new MyDialog.Builder(SettingsActivity.this)
                        .setIcon(R.drawable.ic_dialog_beg)
                        .setMessage("确定要退出登录吗")
                        .setPositiveButton(getString(R.string.text_ok), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                                startActivity(intent);
                                overridePendingTransition(0, 0);
                                MainActivity.instance.finish();
                                finish();
                            }
                        })
                        .setNegativeButton(getString(R.string.text_cancel), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        }).create().show();
                break;
            case R.id.view_settings_close_account:
                closeAccount();
                break;
        }
    }

    private void changePassword() {
        final ChangePasswordDialog.Builder builder = new ChangePasswordDialog.Builder(SettingsActivity.this);
        final ChangePasswordDialog changePasswordDialog = builder.create();
        changePasswordDialog.show();
        builder.getButtonPositive().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String originalPassword = builder.getEditTextOriginalText();
                String newPassword = builder.getEditTextNewText();
                String againPassword = builder.getEditTextAgainText();
                if (TextUtils.isEmpty(originalPassword) || TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(newPassword)) {
                    Toast.makeText(SettingsActivity.this, "还有输入框没有填写", Toast.LENGTH_SHORT).show();
                } else if (!newPassword.equals(againPassword)) {
                    builder.getEditTextNew().setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.drawable_background_password_not_same, null));
                    builder.getEditTextAgain().setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.drawable_background_password_not_same, null));
                    Toast.makeText(SettingsActivity.this, "两次输入密码不一致", Toast.LENGTH_SHORT).show();
                } else if (originalPassword.equals(newPassword)) {
                    builder.getEditTextNew().setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.drawable_background_password_not_same, null));
                    builder.getEditTextOriginal().setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.drawable_background_password_not_same, null));
                    Toast.makeText(SettingsActivity.this, "新密码和旧密码不能相同", Toast.LENGTH_SHORT).show();
                } else {
                    // final ProgressDialog progressDialog = ProgressDialog.show(SettingsActivity.this, null, "正在修改密码…", true, false, null);
                    final MyProgressDialog progressDialog = MyProgressDialog.show(SettingsActivity.this, getString(R.string.text_loading_change_password), false, null);
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", currentUserID);
                    map.put("original_password", new String(Hex.encodeHex(DigestUtils.md5(originalPassword))));
                    map.put("new_password", new String(Hex.encodeHex(DigestUtils.md5(newPassword))));
                    Call call = OkHttpUtils.getInstance().post("changePassword", map);
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(SettingsActivity.this, R.string.error_internet, Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                }
                            });
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String responseString = response.body().string();
                            JSONObject resultJson = JSON.parseObject(responseString);
                            Integer code = resultJson.getInteger("code");
                            if (code == 0) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        builder.getEditTextOriginal().setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.drawable_background_password_not_same, null));
                                        Toast.makeText(SettingsActivity.this, "原密码不正确", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else if (code == 1) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        changePasswordDialog.dismiss();
                                        Toast.makeText(SettingsActivity.this, "修改密码成功！", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                Looper.prepare();
                                Toast.makeText(SettingsActivity.this, "未知错误", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            }
                            progressDialog.dismiss();
                        }
                    });
                }
            }
        });
    }

    private void wipeChatRecord() {
        new MyDialog.Builder(SettingsActivity.this)
                .setIcon(R.drawable.ic_dialog_ask)
                .setMessage("确定要删除所有聊天记录吗？")
                .setPositiveButton(getString(R.string.text_ok), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mSQLiteDatabase = openOrCreateDatabase("ff" + currentUserID + ".db", Context.MODE_PRIVATE, null);
                        mSQLiteDatabase.execSQL("DELETE FROM `message`");
                        mSQLiteDatabase.execSQL("DELETE FROM `chat_record`");
                        // mSQLiteDatabase.delete("message",null,null);
                        // mSQLiteDatabase.delete("chat_record",null,null);
                    }
                })
                .setNegativeButton(getString(R.string.text_cancel), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).create().show();
    }

    private void changeLanguage() {
        final String[] l = {"简体中文", "English"};
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
        builder.setItems(l, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String language = null;
                if (which == 0) {
                    language = "zh";
                } else if (which == 1) {
                    language = "en";
                }

                // final ProgressDialog progressDialog = ProgressDialog.show(SettingsActivity.this, null, "正在关闭Find Friends以应用设置…", true, false, null);
                final MyProgressDialog progressDialog = MyProgressDialog.show(SettingsActivity.this, getString(R.string.text_loading_closing), false, null);

                final String finalLanguage = language;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // 保存设置
                                SharedPreferences sharedPreferences;
                                SharedPreferences.Editor editor;
                                sharedPreferences = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
                                editor = sharedPreferences.edit();
                                editor.putString("language", finalLanguage);
                                editor.apply();

                                MyUtils.updateResources(SettingsActivity.this);

                                progressDialog.dismiss();
                                Toast.makeText(SettingsActivity.this, R.string.text_reboot_to_apply, Toast.LENGTH_SHORT).show();
                                MainActivity.instance.finish();
                                finish();
                            }
                        });
                    }
                }).start();

            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();// 必须要先show再设置宽高
        UIUtils.setItemDialog(dialog, 400);

        // new AlertDialog.Builder(SettingsActivity.this)
        //         .setTitle(R.string.text_chose_language)
        //         .setItems(l, new DialogInterface.OnClickListener() {
        //             @Override
        //             public void onClick(DialogInterface dialog, int which) {
        //
        //
        //
        //             }
        //         })
        //         .setNegativeButton(R.string.text_cancel, null)
        //         .create()
        //         .show();
    }

    @SuppressLint({"SetTextI18n", "ResourceAsColor"})
    private void closeAccount() {
        final int time = 10;
        // String positiveButtonText = getString(R.string.text_ok);
        // String negativeButtonText = getString(R.string.text_cancel);
        MyDialog.Builder builder = new MyDialog.Builder(this);
        builder.setIcon(R.drawable.ic_dialog_warn);
        builder.setMessage("注销账号无法撤销，请谨慎选择！");
        builder.setPositiveButton("不注销", new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        builder.setNegativeButton("注销（" + time + "秒）", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", currentUserID);
                Call call = OkHttpUtils.getInstance().post("closeAccount", map);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Looper.prepare();
                        Toast.makeText(SettingsActivity.this, R.string.error_internet, Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String responseString = response.body().string();
                        JSONObject resultJson = JSON.parseObject(responseString);
                        Integer code = resultJson.getInteger("code");
                        if (code == 0) {
                            Looper.prepare();
                            Toast.makeText(SettingsActivity.this, "未操作数据库", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        } else if (code == 1) {
                            Looper.prepare();
                            Toast.makeText(SettingsActivity.this, "账号已注销，感谢您的使用～", Toast.LENGTH_SHORT).show();
                            // startActivity(new Intent(SettingsActivity.this, LoginActivity.class));
                            MainActivity.instance.finish();
                            finish();
                            Looper.loop();
                        } else {
                            Looper.prepare();
                            Toast.makeText(SettingsActivity.this, "未知错误", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        }
                    }
                });
            }
        });
        MyDialog myDialog = builder.create();
        // myDialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_my);
        myDialog.show();
        final MaterialButton buttonNegative = builder.getButtonNegative();
        buttonNegative.setEnabled(false);
        final ColorStateList backgroundTintList = buttonNegative.getBackgroundTintList();
        buttonNegative.setBackgroundTintList(ColorStateList.valueOf(R.color.colorMyGrayLight));
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = time - 1; i >= 0; i--) {
                    try {
                        Thread.sleep(1000);
                        final int finalI = i;
                        SettingsActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                buttonNegative.setText("注销（" + finalI + "秒）");
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                SettingsActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        buttonNegative.setBackgroundTintList(backgroundTintList);
                        buttonNegative.setText("注销");
                        buttonNegative.setEnabled(true);
                    }
                });
            }
        }).start();
    }

}