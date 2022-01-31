package com.wanfuxiong.findfriends.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class InformationActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {

    private static final String TAG = "InformationActivity";
    private static final int REQUEST_CODE_SIGNATURE = 0;
    private static final String EXTRA_CURRENT_USER_ID = "current_user_id";
    private static final String EXTRA_USER_ID = "user_id";
    private static final String EXTRA_MODE = "mode";// 展示信息的模式，0表示自己，1表示非自己

    private Toolbar mToolbar;
    private TextView mTextViewTitle;
    private ImageView mImageViewUserProfile;
    private ImageView mImageViewEditSignature;
    private TextView mTextViewSignature;
    private LinearLayout mLinearLayoutSignature;
    private TextView mTextViewID;
    private EditText mEditTextUsername;
    private EditText mEditTextInterest;
    private EditText mEditTextPhoneNumber;
    private ImageView mImageViewSex;
    private ImageView mImageViewDial;
    private MaterialButton mMaterialButtonSave;
    private MaterialButton mMaterialButtonChat;

    private boolean isChanged;

    private SQLiteDatabase mSQLiteDatabase;

    private String userIDString;
    private User mCurrentUser;
    private User user;
    private int mode;

    public static Intent newIntent(Context context, String currentUserID, String userID, int mode) {
        Intent intent = new Intent(context, InformationActivity.class);
        intent.putExtra(EXTRA_CURRENT_USER_ID, currentUserID);
        intent.putExtra(EXTRA_USER_ID, userID);
        intent.putExtra(EXTRA_MODE, mode);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);
        Log.d(TAG, "onCreate");

        UIUtils.setStatusBarColor(this, R.color.colorTransparent);
        UIUtils.setStatusBarBackground(this, R.drawable.background_toolbar);

        userIDString = getIntent().getStringExtra(EXTRA_USER_ID);
        mode = getIntent().getIntExtra(EXTRA_MODE, 0);

        // 创建或打开数据库
        mSQLiteDatabase = openOrCreateDatabase("ff" + getIntent().getStringExtra(EXTRA_CURRENT_USER_ID) + ".db", Context.MODE_PRIVATE, null);
        mCurrentUser = SqliteUtils.getCurrentUserInfo(mSQLiteDatabase);
        // 给user赋值
        if (mode == 0) {
            user = SqliteUtils.getCurrentUserInfo(mSQLiteDatabase);
        } else if (mode == 1 || mode == 2) {
            user = SqliteUtils.getUserInfo(mSQLiteDatabase, userIDString);
            Map<String, Object> map = new HashMap<>();
            map.put("id", user.getUserID());

            Call call = OkHttpUtils.getInstance().post("getUserInfo", map);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Toast.makeText(InformationActivity.this, R.string.error_internet, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseString = response.body().string();
                    JSONObject resultJson = JSON.parseObject(responseString);
                    user = resultJson.getObject("user", User.class);
                    SqliteUtils.insertOrUpdateUserInfo(mSQLiteDatabase, user);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setValues();
                        }
                    });
                }
            });
        }

        initView();
        setValues();
        isChanged = false;
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
        mImageViewUserProfile = findViewById(R.id.imageview_information_user_profile);
        mImageViewEditSignature = findViewById(R.id.imageview_information_edit_signature);
        mTextViewSignature = findViewById(R.id.textview_information_signature);
        mTextViewID = findViewById(R.id.textview_information_id);
        mEditTextUsername = findViewById(R.id.edittext_information_username);
        mEditTextInterest = findViewById(R.id.edittext_information_interest);
        mEditTextPhoneNumber = findViewById(R.id.edittext_information_phone_number);
        mImageViewSex = findViewById(R.id.imageview_information_sex);
        mImageViewDial = findViewById(R.id.imageview_information_dial);
        mMaterialButtonSave = findViewById(R.id.button_information_save);
        mMaterialButtonChat = findViewById(R.id.button_information_chat);
        mLinearLayoutSignature = findViewById(R.id.linearlayout_signature);
        mToolbar = findViewById(R.id.toolbar_information);
        mTextViewTitle = findViewById(R.id.textview_information_title);

        // ViewGroup.LayoutParams layoutParams = mToolbar.getLayoutParams();
        // layoutParams.height += UIUtils.getStatusBarHeight(InformationActivity.this);
        // mToolbar.setLayoutParams(layoutParams);
        // mToolbar.setPadding(mToolbar.getPaddingLeft(),
        //         mToolbar.getPaddingTop() + UIUtils.getStatusBarHeight(InformationActivity.this),
        //         mToolbar.getPaddingRight(),
        //         mToolbar.getPaddingBottom());

        mImageViewUserProfile.setOnClickListener(this);
        mImageViewUserProfile.setOnLongClickListener(this);
        mImageViewSex.setOnClickListener(this);
        mMaterialButtonSave.setOnClickListener(this);
        mMaterialButtonChat.setOnClickListener(this);
        mLinearLayoutSignature.setOnClickListener(this);
        mImageViewDial.setOnClickListener(this);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (mode == 0) {
            mImageViewEditSignature.setVisibility(View.VISIBLE);
            mImageViewDial.setVisibility(View.GONE);
            mMaterialButtonChat.setVisibility(View.GONE);
            mEditTextUsername.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    isChanged = true;
                    user.setUserUsername(mEditTextUsername.getText().toString());
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
            mEditTextPhoneNumber.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    isChanged = true;
                    if (mEditTextPhoneNumber.getText().toString().length() == 0) {
                        user.setUserPhoneNumber(null);
                    } else {
                        user.setUserPhoneNumber(mEditTextPhoneNumber.getText().toString());
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
                    isChanged = true;
                    user.setUserInterest(mEditTextInterest.getText().toString());
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        } else if (mode == 1 || mode == 2) {
            mEditTextUsername.setFocusableInTouchMode(false);//不可编辑
            mEditTextUsername.setKeyListener(null);//不可粘贴，长按不会弹出粘贴框
            mEditTextUsername.setClickable(false);//不可点击
            // mEditTextUsername.setFocusable(false);//不可编辑
            // mEditTextUsername.setEnabled(false);

            mEditTextInterest.setFocusableInTouchMode(false);
            mEditTextInterest.setKeyListener(null);
            mEditTextInterest.setClickable(false);

            mEditTextPhoneNumber.setFocusableInTouchMode(false);
            mEditTextPhoneNumber.setKeyListener(null);
            mEditTextPhoneNumber.setClickable(false);

            mImageViewEditSignature.setVisibility(View.GONE);
            mMaterialButtonSave.setVisibility(View.GONE);
            if (mode == 2) {
                mMaterialButtonChat.setVisibility(View.GONE);
            }
            mImageViewSex.setClickable(false);
            mImageViewUserProfile.setClickable(false);

            mLinearLayoutSignature.setClickable(false);
        }

    }

    private void setValues() {
        mTextViewTitle.setText(user.getUserUsername());
        // 头像
        UIUtils.setProfile(user, mImageViewUserProfile);
        // 个性签名
        if (user.getUserSignature() == null) {
            findViewById(R.id.linearlayout_signature).setVisibility(View.GONE);
        } else {
            mTextViewSignature.setText(user.getUserSignature());
        }
        // id
        mTextViewID.setText(userIDString);
        // 用户名
        mEditTextUsername.setText(user.getUserUsername());
        // 性别
        if (user.getUserSex() == 1)
            mImageViewSex.setImageResource(R.drawable.ic_sex_male_checked);
        else
            mImageViewSex.setImageResource(R.drawable.ic_sex_female_checked);
        // 兴趣
        mEditTextInterest.setText(user.getUserInterest());
        // 手机
        if (TextUtils.isEmpty(user.getUserPhoneNumber())) {
            if (mode == 1) {
                mEditTextPhoneNumber.setText("未填写");
            } else if (mode == 0) {
                mEditTextPhoneNumber.setText("");
            }
            mImageViewDial.setVisibility(View.GONE);
        } else {
            mEditTextPhoneNumber.setText(user.getUserPhoneNumber());
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageview_information_dial:
                callPhone(user.getUserPhoneNumber());
                break;
            case R.id.imageview_information_user_profile:
                showChoices();
                break;
            case R.id.linearlayout_signature:
                editSignature();
                break;
            case R.id.button_information_chat:
                Intent chatIntent = ChatActivity.newIntent(InformationActivity.this, mCurrentUser.getUserID().toString(), mCurrentUser.getUserUsername(), userIDString, user.getUserUsername());
                startActivity(chatIntent);
                break;
            case R.id.button_information_save:
                if (mEditTextPhoneNumber.getText().toString().length() != 11 && mEditTextPhoneNumber.getText().toString().length() != 0) {
                    new MyDialog.Builder(InformationActivity.this)
                            .setIcon(R.drawable.ic_dialog_warn)
                            .setMessage("手机号必须是11位！")
                            .setPositiveButton(getString(R.string.text_ok), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            }).create().show();
                } else {
                    new MyDialog.Builder(InformationActivity.this)
                            .setIcon(R.drawable.ic_dialog_ask)
                            .setMessage("确定保存个人信息吗？")
                            .setPositiveButton(getString(R.string.text_ok), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    saveInformation(0);
                                    isChanged = false;
                                }
                            })
                            .setNegativeButton(getString(R.string.text_cancel), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            }).create().show();
                }
                break;
            case R.id.imageview_information_sex:
                new MyDialog.Builder(InformationActivity.this)
                        .setIcon(R.drawable.ic_dialog_ask)
                        .setMessage("确定要更改性别吗？")
                        .setPositiveButton(getString(R.string.text_ok), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                isChanged = true;
                                if (user.getUserSex() == 1) {
                                    mImageViewSex.setImageResource(R.drawable.ic_sex_female_checked);
                                    user.setUserSex(0);
                                    UIUtils.setProfile(user, mImageViewUserProfile);
                                } else {
                                    mImageViewSex.setImageResource(R.drawable.ic_sex_male_checked);
                                    user.setUserSex(1);
                                    UIUtils.setProfile(user, mImageViewUserProfile);
                                }
                            }
                        })
                        .setNegativeButton(getString(R.string.text_cancel), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        })
                        .create()
                        .show();
                break;
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.imageview_information_user_profile:
                showChoices();
                break;
        }
        // 当返回true时，将不会产生连带触发，如点击事件
        return true;
    }

    // 修改头像
    private void changeProfile() {
        isChanged = true;
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, 0x1);
    }

    // 保存头像到本地
    private void saveProfile() {

    }

    // 删除头像
    private void deleteProfile() {
        isChanged = true;
        final String userContent = "id=" + user.getUserID();
        new MyDialog.Builder(InformationActivity.this)
                .setIcon(R.drawable.ic_dialog_ask)
                .setMessage("确定删除头像吗？")
                .setPositiveButton(getString(R.string.text_ok), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        user.setUserProfile(null);
                        UIUtils.setProfile(user, mImageViewUserProfile);
                    }
                })
                .setNegativeButton("再想想", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).create().show();
    }

    // 操作头像
    private void showChoices() {
        final String[] choices1 = {getString(R.string.text_change_profile), getString(R.string.text_save_profile)};
        final String[] choices2 = {getString(R.string.text_change_profile), getString(R.string.text_delete_profile), getString(R.string.text_save_profile)};
        final String[] choices3 = {getString(R.string.text_save_profile)};
        if (mode == 0) {
            if (user.getUserProfile() == null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(InformationActivity.this);
                builder.setItems(choices1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            changeProfile();
                        } else if (which == 1) {
                            saveProfile();
                        }
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();// 必须要先show再设置宽高
                UIUtils.setItemDialog(dialog, 500);
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(InformationActivity.this);
                builder.setItems(choices2, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            changeProfile();
                        } else if (which == 1) {
                            deleteProfile();
                        } else if (which == 2) {
                            saveProfile();
                        }
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();// 必须要先show再设置宽高
                UIUtils.setItemDialog(dialog, 500);
            }
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(InformationActivity.this);
            builder.setItems(choices3, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == 0) {
                        saveProfile();
                    }
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();// 必须要先show再设置宽高
            UIUtils.setItemDialog(dialog, 500);
        }

    }

    // 修改个性签名
    private void editSignature() {
        Intent intent = SignatureActivity.newIntent(InformationActivity.this, user.getUserSignature());
        Log.d("签名 =========== ", user.getUserSignature());
        startActivityForResult(intent, REQUEST_CODE_SIGNATURE);
    }

    // 保存信息
    private void saveInformation(final int saveMode) {
        // final ProgressDialog progressDialog = ProgressDialog.show(this, null, "正在保存信息…", true, false, null);
        final MyProgressDialog progressDialog = MyProgressDialog.show(this, getString(R.string.text_loading_save_info), false, null);
        String json = JSON.toJSONString(user);//关键
        Log.d("json", json);
        Call call = OkHttpUtils.getInstance().post("saveUserInfo", json);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(InformationActivity.this, R.string.error_internet, Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseString = response.body().string();
                JSONObject resultJson = JSON.parseObject(responseString);
                Integer code = resultJson.getInteger("code");
                // 更新sqlite
                SqliteUtils.insertOrUpdateCurrentUserInfo(mSQLiteDatabase, user);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        if (saveMode == 1) {
                            InformationActivity.this.finish();
                        }
                    }
                });
            }
        });
    }

    // 把选择的头像设置为用户头像
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0x1 && resultCode == RESULT_OK) {
            if (data != null) {
                isChanged = true;
                Uri uri = data.getData();
                // 先更新UI
                mImageViewUserProfile.setImageURI(uri);
                mImageViewUserProfile.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mImageViewUserProfile.getWidth()));

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // 获取该图片
                        Bitmap bitmap = ((BitmapDrawable) mImageViewUserProfile.getDrawable()).getBitmap();
                        // 再把获取来的图片转为输入流/字节数组
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                        byte[] bytes = byteArrayOutputStream.toByteArray();

                        user.setUserProfile(bytes);
                    }
                }).start();

            }
        }
        if (requestCode == REQUEST_CODE_SIGNATURE) {
            if (data == null) {
                return;
            }
            String signature = SignatureActivity.getSignatureFromExtra(data);
            mTextViewSignature.setText(signature);
            user.setUserSignature(signature);
            isChanged = true;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // 重写返回键功能
    @Override
    public void onBackPressed() {
        // 如果信息编辑过，那按返回的时候，会弹出对话框
        if (isChanged) {

            new MyDialog.Builder(InformationActivity.this)
                    .setIcon(R.drawable.ic_dialog_ask)
                    .setTitle("更改还未保存")
                    .setMessage("保存更改？")
                    .setPositiveButton("保存", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mEditTextPhoneNumber.getText().toString().length() != 11 && mEditTextPhoneNumber.getText().toString().length() != 0) {
                                new MyDialog.Builder(InformationActivity.this)
                                        .setIcon(R.drawable.ic_dialog_warn)
                                        .setMessage("手机号必须是11位！")
                                        .setPositiveButton(getString(R.string.text_ok), new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                            }
                                        }).create().show();
                            } else {
                                saveInformation(1);
                            }

                        }
                    })
                    .setNegativeButton("不保存", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            InformationActivity.this.finish();
                        }
                    })
                    .setNeutralButton(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    }).create().show();

        } else {
            super.onBackPressed();
        }
    }

    public void callPhone(String phoneNum) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        Uri data = Uri.parse("tel:" + phoneNum);
        intent.setData(data);
        startActivity(intent);
    }
}
