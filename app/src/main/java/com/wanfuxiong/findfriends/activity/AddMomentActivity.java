package com.wanfuxiong.findfriends.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Half;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wanfuxiong.findfriends.R;
import com.wanfuxiong.findfriends.pojo.Moment;
import com.wanfuxiong.findfriends.pojo.User;
import com.wanfuxiong.findfriends.util.OkHttpUtils;
import com.wanfuxiong.findfriends.util.SqliteUtils;
import com.wanfuxiong.findfriends.view.MyDialog;
import com.wanfuxiong.findfriends.view.MyProgressDialog;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AddMomentActivity extends AppCompatActivity {

    private static final String TAG = "AddMomentActivity";
    private static final String EXTRA_CURRENT_USER_ID = "current_user_id";

    private Toolbar mToolbar;
    private EditText mEditText;
    private TextView mTextView;
    private Button mButton;
    private ImageView mImageView;

    private Moment mMoment = new Moment();

    private boolean isImageNull = true;

    public static Intent newIntent(Context context, int currentUserID) {
        Intent intent = new Intent(context, AddMomentActivity.class);
        intent.putExtra(EXTRA_CURRENT_USER_ID, currentUserID);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_moment);

        mMoment.setUserID(getIntent().getIntExtra(EXTRA_CURRENT_USER_ID,0));
        mMoment.setMomentImage(null);
        initView();
    }

    private void initView() {
        mToolbar = findViewById(R.id.toolbar_add_moment);
        mEditText = findViewById(R.id.edittext_add_moment_content);
        mTextView = findViewById(R.id.textview_add_moment_words);
        mButton = findViewById(R.id.button_add_moment_send);
        mImageView = findViewById(R.id.imageview_add_moment_add_image);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mEditText.getText().length() > 40) {
                    mTextView.setText("字数不能超过40！ " + mEditText.getText().length() + "/40");
                    mTextView.setTextColor(Color.RED);
                } else {
                    mTextView.setText(mEditText.getText().length() + "/40");
                    mTextView.setTextColor(getColor(R.color.colorMyGray));
                    mMoment.setMomentContent(mEditText.getText().toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEditText.getText().length() > 40) {
                    new MyDialog.Builder(AddMomentActivity.this)
                            .setMessage("字数不能超过40")
                            .setPositiveButton(getString(R.string.text_ok), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            }).create().show();
                } else {
                    sendMoment();
                }
            }
        });
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addImage();
            }
        });
        mImageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!isImageNull) {
                    new MyDialog.Builder(AddMomentActivity.this)
                            .setMessage("删除图片？")
                            .setPositiveButton(getString(R.string.text_ok), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mImageView.setImageResource(R.drawable.ic_add_2);
                                    mImageView.setPadding(60, 60, 60, 60);
                                    mMoment.setMomentImage(null);
                                    isImageNull = true;
                                }
                            })
                            .setNegativeButton(getString(R.string.text_cancel), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            }).create().show();
                }

                return false;
            }
        });
    }

    private void addImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, 0x1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0x1 && resultCode == RESULT_OK) {
            if (data != null) {
                isImageNull = false;
                Uri uri = data.getData();
                mImageView.setImageURI(uri);
                mImageView.setPadding(0, 0, 0, 0);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // 获取该图片
                        Bitmap bitmap = ((BitmapDrawable) mImageView.getDrawable()).getBitmap();
                        // 再把获取来的图片转为输入流/字节数组
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                        byte[] bytes = byteArrayOutputStream.toByteArray();

                        mMoment.setMomentImage(bytes);
                    }
                }).start();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void sendMoment() {
        mMoment.setSendDate(new Date());
        final MyProgressDialog progressDialog = MyProgressDialog.show(this, "正在发布……", false, null);
        String json = JSON.toJSONString(mMoment);//关键
        Log.d("json", json);
        Call call = OkHttpUtils.getInstance().post("sendMoment", json);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(AddMomentActivity.this, R.string.error_internet, Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseString = response.body().string();
                JSONObject resultJson = JSON.parseObject(responseString);
                final int code = resultJson.getIntValue("code");
                // 更新sqlite

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        if (code == 1) {
                            SqliteUtils.insertCurrentUserMoment(openOrCreateDatabase("ff" + getIntent().getIntExtra(EXTRA_CURRENT_USER_ID,0) + ".db", Context.MODE_PRIVATE, null),mMoment);
                            AddMomentActivity.this.finish();
                        } else {
                            new MyDialog.Builder(AddMomentActivity.this)
                                    .setMessage("发布失败！")
                                    .setPositiveButton(getString(R.string.text_ok), new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                        }
                                    }).create().show();
                        }
                    }
                });
            }
        });
    }

}