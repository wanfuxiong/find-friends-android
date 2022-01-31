package com.wanfuxiong.findfriends.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.CornerFamily;
import com.wanfuxiong.findfriends.R;
import com.wanfuxiong.findfriends.pojo.Message;
import com.wanfuxiong.findfriends.pojo.Moment;
import com.wanfuxiong.findfriends.pojo.User;
import com.wanfuxiong.findfriends.util.MyUtils;
import com.wanfuxiong.findfriends.util.MyWebSocket;
import com.wanfuxiong.findfriends.util.OkHttpUtils;
import com.wanfuxiong.findfriends.util.SqliteUtils;
import com.wanfuxiong.findfriends.util.UIUtils;
import com.wanfuxiong.findfriends.view.MyDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity implements View.OnKeyListener {

    public static final String EXTRA_WEB_SOCKET = "web_socket";
    public static final String EXTRA_FROM_USER_ID = "from_user_id";
    public static final String EXTRA_FROM_USER_USERNAME = "from_user_username";
    public static final String EXTRA_TO_USER_ID = "to_user_id";
    public static final String EXTRA_TO_USER_USERNAME = "to_user_username";

    private static final String TAG = "ChatActivity";

    private Toolbar mToolbar;
    private TextView mTextViewTitle;
    private RecyclerView mRecyclerViewChat;
    private Button mButtonSend;
    private EditText mEditTextMessage;

    private MyWebSocket myWebSocketClient;

    private String fromUserID;
    private String fromUserUsername;
    private String toUserID;
    private String toUserUsername;

    private User fromUser;
    private User toUser;

    private List<Message> mMessageList = new ArrayList<>();

    private ChatAdapter mAdapter;

    private SQLiteDatabase mSQLiteDatabase;

    public static Intent newIntent(Context context, String fromUserID, String fromUserUsername, String toUserID, String toUserUsername) {
        Intent intent = new Intent(context, ChatActivity.class);
        // intent.putExtra(EXTRA_WEB_SOCKET, myWebSocket);
        intent.putExtra(EXTRA_FROM_USER_ID, fromUserID);
        intent.putExtra(EXTRA_FROM_USER_USERNAME, fromUserUsername);
        intent.putExtra(EXTRA_TO_USER_ID, toUserID);
        intent.putExtra(EXTRA_TO_USER_USERNAME, toUserUsername);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Log.d(TAG, "onCreate");

        UIUtils.setStatusBarColor(this, R.color.colorTransparent);
        UIUtils.setStatusBarBackground(this, R.drawable.background_toolbar);

        myWebSocketClient = MainActivity.myWebSocketClient;

        fromUserID = getIntent().getStringExtra(EXTRA_FROM_USER_ID);
        fromUserUsername = getIntent().getStringExtra(EXTRA_FROM_USER_USERNAME);
        toUserID = getIntent().getStringExtra(EXTRA_TO_USER_ID);
        toUserUsername = getIntent().getStringExtra(EXTRA_TO_USER_USERNAME);

        // 注册EventBus事件
        EventBus.getDefault().register(this);

        initView();

        // 创建或打开数据库
        mSQLiteDatabase = openOrCreateDatabase("ff" + fromUserID + ".db", Context.MODE_PRIVATE, null);

        fromUser = SqliteUtils.getCurrentUserInfo(mSQLiteDatabase);
        toUser = SqliteUtils.getUserInfo(mSQLiteDatabase, toUserID);

        // 更新对方信息
        // 判断对方是否已经在message表中
        boolean isToUserExists = false;
        Cursor cursor = mSQLiteDatabase.query("message", null, null, null, null, null, null);
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            Log.d(TAG, toUserID + "   " + cursor.getInt(0));
            if (Integer.parseInt(toUserID) == cursor.getInt(0)) {
                isToUserExists = true;
                Log.d(TAG, "onStart 在message表中找到了该用户");
                // 首先需要获取该用户信息
                Map<String, Object> map = new HashMap<>();
                map.put("id", Integer.parseInt(toUserID));// 获取发件人（fromuserid）的信息
                Call call = OkHttpUtils.getInstance().post("getUserInfo", map);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Looper.prepare();
                        Toast.makeText(ChatActivity.this, R.string.error_internet, Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String responseString = response.body().string();
                        JSONObject resultJson = JSON.parseObject(responseString);
                        User user = resultJson.getObject("user", User.class);
                        if (user != null) {
                            // 先把缓存user的表给更新一下
                            SqliteUtils.insertOrUpdateUserInfo(mSQLiteDatabase, user);
                            // 再去更新message表中的user数据以在message页面展示用户的最新数据
                            ContentValues cv = new ContentValues();
                            cv.put("user_username", user.getUserUsername());
                            cv.put("user_sex", user.getUserSex());
                            cv.put("user_profile", user.getUserProfile());
                            mSQLiteDatabase.update("message", cv, "user_id=?", new String[]{user.getUserID().toString()});
                        } else {
                            // 删除与该用户的聊天记录
                            // mSQLiteDatabase.delete("message", "user_id=?", new String[]{toUserID});
                            // mSQLiteDatabase.delete("chat_record", "from_user_id=? AND to_user_id=?", new String[]{toUserID, fromUserID});
                            // mSQLiteDatabase.delete("chat_record", "from_user_id=? AND to_user_id=?", new String[]{fromUserID, toUserID});
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    new MyDialog.Builder(ChatActivity.this)
                                            .setIcon(R.drawable.ic_dialog_ask)
                                            .setMessage((toUser.getUserSex() == 0 ? "她" : "他") + "已经离开星球啦～")
                                            .setPositiveButton(String.valueOf(getText(R.string.text_ok)), new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    finish();
                                                }
                                            })
                                            .create()
                                            .show();
                                }
                            });
                        }
                    }
                });
                break;
            }
            cursor.moveToNext();
        }
        // 插入对方信息（上面没找到）
        if (!isToUserExists) {
            Log.d(TAG, "onStart message表无该用户，正在插入该用户");
            User user = SqliteUtils.getUserInfo(mSQLiteDatabase, toUserID);
            ContentValues cv = new ContentValues();
            cv.put("user_id", user.getUserID());
            cv.put("user_username", toUserUsername);
            cv.put("user_sex", user.getUserSex());
            cv.put("message_data", "");
            cv.put("user_profile", user.getUserProfile());
            mSQLiteDatabase.insert("message", null, cv);
        }

        // 加载历史消息记录
        Log.d(TAG, "正在加载历史聊天记录");
        Cursor cursor2 = mSQLiteDatabase.query("chat_record", null, null, null, null, null, null);
        cursor2.moveToFirst();
        for (int i = 0; i < cursor2.getCount(); i++) {
            // 别人发给我的消息 和 我给别人发的消息
            if (cursor2.getInt(0) == Integer.parseInt(toUserID) || cursor2.getInt(1) == Integer.parseInt(toUserID)) {
                mMessageList.add(new Message(
                        cursor2.getInt(1),
                        cursor2.getInt(0),
                        cursor2.getString(2),
                        MyUtils.convertStringToDate(cursor2.getString(3))
                ));
            }
            cursor2.moveToNext();
        }
        cursor2.close();
        updateUI();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initView() {
        mToolbar = findViewById(R.id.toolbar_chat);
        mTextViewTitle = findViewById(R.id.textview_chat_title);
        mRecyclerViewChat = findViewById(R.id.recyclerview_chat);
        mEditTextMessage = findViewById(R.id.edittext_chat_message);
        mButtonSend = findViewById(R.id.button_chat_send);

        mRecyclerViewChat.setLayoutManager(new LinearLayoutManager(ChatActivity.this));
        mRecyclerViewChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "点击了recyclerview");
            }
        });

        mTextViewTitle.setText(toUserUsername);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        // ViewGroup.LayoutParams layoutParams = mToolbar.getLayoutParams();
        // layoutParams.height += UIUtils.getStatusBarHeight(InformationActivity.this);
        // mToolbar.setLayoutParams(layoutParams);
        // mToolbar.setPadding(mToolbar.getPaddingLeft(),
        //         mToolbar.getPaddingTop() + UIUtils.getStatusBarHeight(InformationActivity.this),
        //         mToolbar.getPaddingRight(),
        //         mToolbar.getPaddingBottom());

        mButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
        //点击软键盘外部，收起软键盘
        mEditTextMessage.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    Log.d(TAG, "mEditTextMessage !hasFocus");
                    InputMethodManager manager = ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE));
                    if (manager != null)
                        manager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                } else {
                    Log.d(TAG, "mEditTextMessage hasFocus");

                }
                // 滑动到底部
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // 滑动到底部
                                mRecyclerViewChat.scrollToPosition(mMessageList.size() - 1);
                            }
                        });
                    }
                }).start();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");

        // // 更新对方信息
        // // 判断对方是否已经在message表中
        // boolean isToUserExists = false;
        // Cursor cursor = mSQLiteDatabase.query("message", null, null, null, null, null, null);
        // cursor.moveToFirst();
        // for (int i = 0; i < cursor.getCount(); i++) {
        //     Log.d(TAG, toUserID + "   " + cursor.getInt(0));
        //     if (Integer.parseInt(toUserID) == cursor.getInt(0)) {
        //         isToUserExists = true;
        //         Log.d(TAG, "onStart 在message表中找到了该用户");
        //         // 首先需要获取该用户信息
        //         Map<String, Object> map = new HashMap<>();
        //         map.put("id", Integer.parseInt(toUserID));// 获取发件人（fromuserid）的信息
        //         Call call = OkHttpUtils.getInstance().post("getUserInfo", map);
        //         call.enqueue(new Callback() {
        //             @Override
        //             public void onFailure(Call call, IOException e) {
        //                 Looper.prepare();
        //                 Toast.makeText(ChatActivity.this, R.string.error_internet, Toast.LENGTH_SHORT).show();
        //                 Looper.loop();
        //             }
        //
        //             @Override
        //             public void onResponse(Call call, Response response) throws IOException {
        //                 String responseString = response.body().string();
        //                 JSONObject resultJson = JSON.parseObject(responseString);
        //                 User user = resultJson.getObject("user", User.class);
        //                 if (user != null) {
        //                     // 先把缓存user的表给更新一下
        //                     SqliteUtils.insertOrUpdateUserInfo(mSQLiteDatabase, user);
        //                     // 再去更新message表中的user数据以在message页面展示用户的最新数据
        //                     ContentValues cv = new ContentValues();
        //                     cv.put("user_username", user.getUserUsername());
        //                     cv.put("user_sex", user.getUserSex());
        //                     cv.put("user_profile", user.getUserProfile());
        //                     mSQLiteDatabase.update("message", cv, "user_id=?", new String[]{user.getUserID().toString()});
        //                 } else {
        //                     // 删除与该用户的聊天记录
        //                     // mSQLiteDatabase.delete("message", "user_id=?", new String[]{toUserID});
        //                     // mSQLiteDatabase.delete("chat_record", "from_user_id=? AND to_user_id=?", new String[]{toUserID, fromUserID});
        //                     // mSQLiteDatabase.delete("chat_record", "from_user_id=? AND to_user_id=?", new String[]{fromUserID, toUserID});
        //                     runOnUiThread(new Runnable() {
        //                         @Override
        //                         public void run() {
        //                             new MyDialog.Builder(ChatActivity.this)
        //                                     .setIcon(R.drawable.ic_dialog_smile)
        //                                     .setMessage(toUser.getUserSex() == 0 ? "她" : "他" + "已经离开星球啦～")
        //                                     .setPositiveButton(String.valueOf(getText(R.string.text_ok)), new View.OnClickListener() {
        //                                         @Override
        //                                         public void onClick(View v) {
        //                                             finish();
        //                                         }
        //                                     })
        //                                     .create()
        //                                     .show();
        //                         }
        //                     });
        //                 }
        //             }
        //         });
        //         break;
        //     }
        //     cursor.moveToNext();
        // }
        // // 插入对方信息（上面没找到）
        // if (!isToUserExists) {
        //     Log.d(TAG, "onStart message表无该用户，正在插入该用户");
        //     User user = SqliteUtils.getUserInfo(mSQLiteDatabase, toUserID);
        //     ContentValues cv = new ContentValues();
        //     cv.put("user_id", user.getUserID());
        //     cv.put("user_username", toUserUsername);
        //     cv.put("user_sex", user.getUserSex());
        //     cv.put("message_data", "");
        //     cv.put("user_profile", user.getUserProfile());
        //     mSQLiteDatabase.insert("message", null, cv);
        // }
        //
        // // 加载历史消息记录
        // Log.d(TAG, "正在加载历史聊天记录");
        // Cursor cursor2 = mSQLiteDatabase.query("chat_record", null, null, null, null, null, null);
        // cursor2.moveToFirst();
        // for (int i = 0; i < cursor2.getCount(); i++) {
        //     // 别人发给我的消息 和 我给别人发的消息
        //     if (cursor2.getInt(0) == Integer.parseInt(toUserID) || cursor2.getInt(1) == Integer.parseInt(toUserID)) {
        //         mMessageList.add(new Message(
        //                 cursor2.getInt(1),
        //                 cursor2.getInt(0),
        //                 cursor2.getString(2),
        //                 MyUtils.convertStringToDate(cursor2.getString(3))
        //         ));
        //     }
        //     cursor2.moveToNext();
        // }
        // cursor2.close();
        // updateUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        // 解除EventBus注册
        EventBus.getDefault().unregister(this);
    }

    public void sendMessage() {
        String input = mEditTextMessage.getText().toString();
        if (!TextUtils.isEmpty(input)) {
            Message message = new Message();

            message.setToUserID(Integer.valueOf(toUserID));

            message.setFromUserID(Integer.valueOf(fromUserID));
            message.setMessageData(input);
            message.setDate(new Date());

            // 转化成JSON格式提交
            myWebSocketClient.send(JSON.toJSONString(message));
            Log.i(TAG, "发送消息" + JSON.toJSONString(message));

            mEditTextMessage.setText("");
            updateUI();
        }
    }

    // EventBus接收事件
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateMsgView(Message message) {
        Log.d(TAG, "EventBus");
        if (message.getFromUserID() != 0) {
            mMessageList.add(message);
            updateUI();
            SqliteUtils.setMessageRead(mSQLiteDatabase, Integer.parseInt(toUserID));
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (v.getId() == R.id.edittext_chat_message) {
            // if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                sendMessage();
                return true;
            }
        }
        return false;
    }

    private void updateUI() {
        if (mAdapter == null) {
            mAdapter = new ChatAdapter(mMessageList);
            mRecyclerViewChat.setAdapter(mAdapter);
        } else {
            mAdapter.notifyDataSetChanged();
        }
        // 滑动到底部
        mRecyclerViewChat.scrollToPosition(mMessageList.size() - 1);
    }

    // ViewHolder
    private class ChatHolder extends RecyclerView.ViewHolder {

        private Message mMessage;

        private LinearLayout mLinearLayout;
        // private TextView mTextViewMessage;

        public ChatHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_chat, parent, false));

            mLinearLayout = itemView.findViewById(R.id.view_chat_message);
            // mTextViewMessage = itemView.findViewById(R.id.textview_chat_message_data);
        }

        public void bind(Message message) {
            mLinearLayout.removeAllViews();// 重要！！！
            mMessage = message;

            int chatProfileWidth = getResources().getDimensionPixelOffset(R.dimen.dp48);
            int chatProfileRadius = getResources().getDimensionPixelOffset(R.dimen.dp24);
            int dp8 = getResources().getDimensionPixelOffset(R.dimen.dp8);
            int dp16 = getResources().getDimensionPixelOffset(R.dimen.dp16);
            int dp20 = getResources().getDimensionPixelOffset(R.dimen.dp20);
            int dp24 = getResources().getDimensionPixelOffset(R.dimen.dp24);
            int dp26 = getResources().getDimensionPixelOffset(R.dimen.dp26);
            int dp32 = getResources().getDimensionPixelOffset(R.dimen.dp32);
            int dp360 = getResources().getDimensionPixelOffset(R.dimen.dp360);

            float cornerSize = (float) chatProfileRadius;
            TextView mTextViewMessage = new TextView(ChatActivity.this);
            ShapeableImageView imageView = new ShapeableImageView(ChatActivity.this);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setBackgroundResource(R.color.colorPrimaryLight);
            imageView.setShapeAppearanceModel(imageView.getShapeAppearanceModel().toBuilder().setAllCorners(CornerFamily.ROUNDED, cornerSize).build());

            // 聊天消息
            mTextViewMessage.setText(mMessage.getMessageData());

            // 系统消息
            // mLinearLayout.setGravity(Gravity.CENTER);
            // if (mMessage.getFromUserID().equals("0000")) {
            //     mLinearLayout.setGravity(Gravity.CENTER);
            //     mTextViewMessage.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.drawable_background_chat_warn, null));
            //     mTextViewMessage.setPadding(12, 4, 12, 4);
            //     mTextViewMessage.setTextSize(10);
            //     mTextViewMessage.setTextColor(Color.WHITE);
            // } else
            if (mMessage.getFromUserID() == Integer.parseInt(toUserID)) {
                mLinearLayout.setGravity(Gravity.START);

                UIUtils.setProfile(toUser, imageView);
                LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(chatProfileWidth, chatProfileWidth);
                imageParams.setMargins(dp16, dp16, 0, dp16);
                mLinearLayout.addView(imageView, imageParams);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(MomentActivity.newIntent(ChatActivity.this, Integer.parseInt(fromUserID), Integer.parseInt(toUserID)));
                    }
                });

                LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                textParams.setMargins(dp8, dp24, dp16, dp16);
                mTextViewMessage.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.drawable_background_chat, null));
                mTextViewMessage.setPadding(dp16, dp8, dp16, dp8);
                mTextViewMessage.setTextColor(Color.BLACK);
                mTextViewMessage.setMaxWidth(dp360);
                mLinearLayout.addView(mTextViewMessage, textParams);

            } else if (mMessage.getFromUserID() == Integer.parseInt(fromUserID)) {// 是自己
                mLinearLayout.setGravity(Gravity.END);

                LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                textParams.setMargins(dp16, dp24, dp8, dp16);
                mTextViewMessage.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.drawable_background_chat_me, null));
                mTextViewMessage.setPadding(dp16, dp8, dp16, dp8);
                mTextViewMessage.setTextColor(Color.BLACK);
                mTextViewMessage.setMaxWidth(dp360);
                mLinearLayout.addView(mTextViewMessage, textParams);

                UIUtils.setProfile(fromUser, imageView);
                LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(chatProfileWidth, chatProfileWidth);
                imageParams.setMargins(0, dp16, dp16, dp16);
                mLinearLayout.addView(imageView, imageParams);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(InformationActivity.newIntent(ChatActivity.this, fromUserID, fromUserID, 0));
                        // startActivity(MomentActivity.newIntent(ChatActivity.this, Integer.parseInt(fromUserID), Integer.parseInt(fromUserID)));
                    }
                });
            }
        }
    }

    // Adapter
    private class ChatAdapter extends RecyclerView.Adapter<ChatHolder> {

        private List<Message> mMessages;

        public ChatAdapter(List<Message> messages) {
            mMessages = messages;
        }

        @NonNull
        @Override
        public ChatHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(ChatActivity.this);
            return new ChatHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull ChatHolder holder, int position) {
            Message message = mMessages.get(position);
            holder.bind(message);
        }

        @Override
        public int getItemCount() {
            return mMessages.size();
        }
    }

}