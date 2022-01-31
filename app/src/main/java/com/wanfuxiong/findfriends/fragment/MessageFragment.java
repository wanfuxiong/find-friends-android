package com.wanfuxiong.findfriends.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.wanfuxiong.findfriends.R;
import com.wanfuxiong.findfriends.activity.ChatActivity;
import com.wanfuxiong.findfriends.activity.SearchActivity;
import com.wanfuxiong.findfriends.pojo.Message;
import com.wanfuxiong.findfriends.pojo.User;
import com.wanfuxiong.findfriends.util.MyUtils;
import com.wanfuxiong.findfriends.util.SqliteUtils;
import com.wanfuxiong.findfriends.util.UIUtils;
import com.wanfuxiong.findfriends.view.MyDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class MessageFragment extends Fragment {

    private static final String TAG = "MessageFragment";
    private static final String EXTRA_CURRENT_USER_ID = "current_user_id";
    private static final String EXTRA_CURRENT_USER_USERNAME = "current_user_username";

    private Toolbar mToolbar;
    private ConstraintLayout mConstraintLayoutHeader;
    private TextView mTextViewTitle;
    private RecyclerView mRecyclerViewMessage;
    private FloatingActionButton mButtonSearch;

    private SQLiteDatabase mSQLiteDatabase;

    private static int currentUserID;
    private static String currentUserUsername;
    private static User currentUser;
    private static List<User> mUserList;

    private MessageAdapter mAdapter;

    private static int messageCount;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        currentUserID = getActivity().getIntent().getIntExtra(EXTRA_CURRENT_USER_ID, 0);

        mUserList = new ArrayList<>();

        // 创建或打开数据库
        mSQLiteDatabase = getActivity().openOrCreateDatabase("ff" + currentUserID + ".db", Context.MODE_PRIVATE, null);

        currentUser = SqliteUtils.getCurrentUserInfo(mSQLiteDatabase);
        currentUserUsername = currentUser.getUserUsername();

        // 注册EventBus事件
        Log.d(TAG,"注册EventBus事件");
        EventBus.getDefault().register(this);

    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View root = inflater.inflate(R.layout.fragment_message, container, false);

        int statusBarHeight = UIUtils.getStatusBarHeight(getContext());

        // root.setPadding(0, UIUtils.getStatusBarHeight(getContext()), 0, 0);

        mRecyclerViewMessage = root.findViewById(R.id.recyclerview_message);
        mRecyclerViewMessage.setLayoutManager(new LinearLayoutManager(getActivity()));

        // mConstraintLayoutHeader = root.findViewById(R.id.constraintlayout_message_header);
        // ViewGroup.LayoutParams layoutParams = mConstraintLayoutHeader.getLayoutParams();
        // layoutParams.height += layoutParams.height;
        // mConstraintLayoutHeader.setLayoutParams(layoutParams);

        mToolbar = root.findViewById(R.id.toolbar_message);
        ViewGroup.LayoutParams layoutParams = mToolbar.getLayoutParams();
        layoutParams.height += UIUtils.getStatusBarHeight(getContext());
        mToolbar.setLayoutParams(layoutParams);
        mToolbar.setPadding(mToolbar.getPaddingLeft(),
                mToolbar.getPaddingTop() + UIUtils.getStatusBarHeight(getContext()),
                mToolbar.getPaddingRight(),
                mToolbar.getPaddingBottom());

        mTextViewTitle = root.findViewById(R.id.textview_message_title);

        mButtonSearch = root.findViewById(R.id.button_message_search);
        mButtonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(SearchActivity.newIntent(getContext(), currentUserID, currentUserUsername));
            }
        });

        return root;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        UIUtils.setAndroidNativeLightStatusBar(getActivity(), false);
        mUserList.clear();
        messageCount = 0;
        mUserList.addAll(SqliteUtils.getMessageList(mSQLiteDatabase));
        updateUI();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateMsgView(final Message message) {
        Log.d(TAG, "EventBus");
        if (message.getFromUserID() != 0) {
            messageCount=0;
            boolean isToUserExists = false;// 用来暂时标记message表中是否存在该用户
            Cursor cursor = mSQLiteDatabase.query("message", null, null, null, null, null, null);
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                // 去找message表中是否存在该用户（该用户不能是自己）
                if (message.getFromUserID() == cursor.getInt(0) && currentUserID != (message.getFromUserID())) {
                    // 接收到了一个用户消息，该用户已经在message表中，直接刷新消息页面
                    isToUserExists = true;
                    mUserList.clear();
                    mUserList.addAll(SqliteUtils.getMessageList(mSQLiteDatabase));
                    updateUI();
                    break;
                } else if (message.getToUserID() == cursor.getInt(0) && currentUserID == message.getFromUserID()) {
                    // 接收到了自己发给别人的消息，直接刷新消息页面
                    isToUserExists = true;
                    mUserList.clear();
                    mUserList.addAll(SqliteUtils.getMessageList(mSQLiteDatabase));
                    updateUI();
                    break;
                }
                cursor.moveToNext();
            }
            if (!isToUserExists) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mUserList.clear();
                                mUserList.addAll(SqliteUtils.getMessageList(mSQLiteDatabase));
                                updateUI();
                            }
                        });
                    }
                }).start();
            }
        }

        Log.d(TAG, "EventBus 已完成所有sqlite操作");
    }

    private void updateUI() {
        if (mAdapter == null) {
            mAdapter = new MessageAdapter(mUserList);
            mRecyclerViewMessage.setAdapter(mAdapter);
        } else {
            mAdapter.notifyDataSetChanged();
        }
    }

    // ViewHolder
    private class MessageHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private User mUser;

        private ConstraintLayout mConstraintLayout;
        private TextView mTextViewUnread;
        private TextView mTextViewUsername;
        private TextView mTextViewMessage;
        private TextView mTextViewDate;
        private ImageView mImageViewUserProfile;

        public MessageHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_message, parent, false));
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

            mConstraintLayout = itemView.findViewById(R.id.constraintlayout_message_unread);
            mTextViewUnread = itemView.findViewById(R.id.textview_message_unread);
            mTextViewUsername = itemView.findViewById(R.id.textview_message_user_username);
            mTextViewMessage = itemView.findViewById(R.id.textview_message_data);
            mTextViewDate = itemView.findViewById(R.id.textview_message_date);
            mImageViewUserProfile = itemView.findViewById(R.id.imageview_message_user_profile);
        }

        @Override
        public void onClick(View v) {
            SqliteUtils.setMessageRead(mSQLiteDatabase, mUser.getUserID());
            Intent intent = ChatActivity.newIntent(
                    getActivity(),
                    String.valueOf(currentUserID),
                    currentUserUsername,
                    mUser.getUserID().toString(),
                    mUser.getUserUsername()
            );
            startActivity(intent);
        }

        @SuppressLint("SetTextI18n")
        public void bind(User user) {
            mUser = user;
            mTextViewUsername.setText(mUser.getUserUsername());
            UIUtils.setProfile(user, mImageViewUserProfile);

            // 去找该user对应的聊天记录
            Cursor cursor = mSQLiteDatabase.query("message", null, null, null, null, null, null);
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                if (user.getUserID() == cursor.getInt(0)) {
                    Log.d(TAG, "在message表中找到了与" + user.getUserID() + "最新的一条聊天记录");
                    mTextViewMessage.setText(cursor.getString(3));
                    // if (!TextUtils.isEmpty(cursor.getString(3))) {// 如果没发过消息，仅仅是点开来看了一下，那就不设置发送/接收消息时间
                    mTextViewDate.setText(MyUtils.dateFormate(getContext(), cursor.getString(5)));
                    // }
                    if (cursor.getInt(6) == 0) {
                        mConstraintLayout.setVisibility(View.GONE);
                    } else if (cursor.getInt(6) < 100) {
                        mTextViewUnread.setText(String.valueOf(cursor.getInt(6)));
                        mConstraintLayout.setVisibility(View.VISIBLE);
                    } else {
                        mTextViewUnread.setText("···");
                        mConstraintLayout.setVisibility(View.VISIBLE);
                    }
                    messageCount = messageCount + cursor.getInt(6);
                    if (messageCount == 0) {
                        mTextViewTitle.setText(getString(R.string.title_message));
                    } else {
                        mTextViewTitle.setText(getString(R.string.title_message) + " (" + messageCount + ")");
                    }
                    break;
                }
                cursor.moveToNext();
            }
            cursor.close();
        }

        @Override
        public boolean onLongClick(View v) {
            Log.d(TAG, "长按");
            new MyDialog.Builder(getActivity())
                    .setIcon(R.drawable.ic_dialog_ask)
                    .setMessage("删除和" + (mUser.getUserSex() == 0 ? "她" : "他") + "消息记录？")
                    .setPositiveButton(getString(R.string.text_ok), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            deleteChatRecord();
                        }
                    })
                    .setNegativeButton(getString(R.string.text_cancel), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    }).create().show();
            return true;
        }

        private void deleteChatRecord() {
            mSQLiteDatabase.delete("message", "user_id=?", new String[]{mUser.getUserID().toString()});
            mSQLiteDatabase.delete("chat_record", "from_user_id=? AND to_user_id=?", new String[]{mUser.getUserID().toString(), String.valueOf(currentUserID)});
            mSQLiteDatabase.delete("chat_record", "from_user_id=? AND to_user_id=?", new String[]{String.valueOf(currentUserID), mUser.getUserID().toString()});
            mUserList.clear();
            mUserList.addAll(SqliteUtils.getMessageList(mSQLiteDatabase));
            updateUI();
        }
    }

    // Adapter
    private class MessageAdapter extends RecyclerView.Adapter<MessageHolder> {

        private List<User> mUsers;

        public MessageAdapter(List<User> users) {
            mUsers = users;
        }

        @NonNull
        @Override
        public MessageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new MessageHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull MessageHolder holder, int position) {
            User user = mUsers.get(position);
            holder.bind(user);
        }

        @Override
        public int getItemCount() {
            return mUsers.size();
        }
    }
}
