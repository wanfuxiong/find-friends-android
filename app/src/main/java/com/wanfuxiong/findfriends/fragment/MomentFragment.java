package com.wanfuxiong.findfriends.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.wanfuxiong.findfriends.R;
import com.wanfuxiong.findfriends.activity.AddMomentActivity;
import com.wanfuxiong.findfriends.activity.ChatActivity;
import com.wanfuxiong.findfriends.activity.InformationActivity;
import com.wanfuxiong.findfriends.activity.SettingsActivity;
import com.wanfuxiong.findfriends.pojo.Moment;
import com.wanfuxiong.findfriends.pojo.User;
import com.wanfuxiong.findfriends.util.MyUtils;
import com.wanfuxiong.findfriends.util.OkHttpUtils;
import com.wanfuxiong.findfriends.util.SqliteUtils;
import com.wanfuxiong.findfriends.util.UIUtils;
import com.wanfuxiong.findfriends.view.MyDialog;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MomentFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "MomentFragment";
    // private static final String ARG_CURRENT_USER_ID = "current_user_id";
    // private static final String ARG_USER_ID = "user_id";
    private final static String EXTRA_CURRENT_USER_ID = "current_user_id";
    private final static String EXTRA_USER_ID = "user_id";

    private RecyclerView mRecyclerView;
    private View mViewHeader;
    private TextView mTextViewSettings;
    private TextView mTextViewUsername;
    private ShapeableImageView mImageViewUserProfile;
    private FloatingActionButton mButtonAddMoment;
    private FloatingActionButton mButtonChat;

    private User mUser;// 要展示朋友圈的用户
    private User mCurrentUser;// 当前登录的用户
    private SQLiteDatabase mSQLiteDatabase;
    private MomentAdapter mAdapter;
    private List<Moment> mMomentList;
    private boolean mIsCurrentUser;

    // public static MomentFragment newInstance(int currentUserID, int userID) {
    //     Bundle args = new Bundle();
    //     args.putSerializable(ARG_CURRENT_USER_ID, currentUserID);
    //     args.putSerializable(ARG_USER_ID, userID);
    //
    //     MomentFragment fragment = new MomentFragment();
    //     fragment.setArguments(args);
    //     return fragment;
    // }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        mMomentList = new ArrayList<>();

        int currentUserID = getActivity().getIntent().getIntExtra(EXTRA_CURRENT_USER_ID, 0);
        int userID = getActivity().getIntent().getIntExtra(EXTRA_USER_ID, 0);

        mSQLiteDatabase = getActivity().openOrCreateDatabase("ff" + currentUserID + ".db", Context.MODE_PRIVATE, null);
        mCurrentUser = SqliteUtils.getCurrentUserInfo(mSQLiteDatabase);
        if (userID == 0) {
            mUser = SqliteUtils.getCurrentUserInfo(mSQLiteDatabase);
            mIsCurrentUser = true;
        } else {
            mUser = SqliteUtils.getUserInfo(mSQLiteDatabase, String.valueOf(userID));
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View root = inflater.inflate(R.layout.fragment_moment, container, false);

        mTextViewSettings = root.findViewById(R.id.textview_moment_settings);
        mTextViewUsername = root.findViewById(R.id.textview_moment_username);
        mImageViewUserProfile = root.findViewById(R.id.imageview_moment_profile);
        mButtonAddMoment = root.findViewById(R.id.button_moment_add);
        mButtonChat = root.findViewById(R.id.button_moment_chat);
        mRecyclerView = root.findViewById(R.id.recyclerview_moment_moment);
        mViewHeader = root.findViewById(R.id.constraintlayout_moment_header);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mTextViewUsername.setText(mUser.getUserUsername());
        UIUtils.setProfile(mUser, mImageViewUserProfile);
        mTextViewSettings.setOnClickListener(this);
        mImageViewUserProfile.setOnClickListener(this);
        mButtonAddMoment.setOnClickListener(this);
        mButtonChat.setOnClickListener(this);
        mViewHeader.setOnClickListener(this);

        ViewGroup.LayoutParams layoutParams = mViewHeader.getLayoutParams();
        layoutParams.height += UIUtils.getStatusBarHeight(getActivity());
        mViewHeader.setLayoutParams(layoutParams);

        if (!mIsCurrentUser) {
            mTextViewSettings.setVisibility(View.GONE);
            mButtonAddMoment.setVisibility(View.GONE);
            mButtonChat.setVisibility(View.VISIBLE);
        }

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        getMomentFromSqliteAndUpdateUI();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.textview_moment_settings:
                startActivity(SettingsActivity.newIntent(getActivity(), mUser.getUserID()));
                break;
            case R.id.imageview_moment_profile:
                startActivity(
                        InformationActivity.newIntent(
                                getActivity(),
                                String.valueOf(getActivity().getIntent().getIntExtra(EXTRA_CURRENT_USER_ID, 0)),
                                String.valueOf(mUser.getUserID()),
                                mIsCurrentUser ? 0 : 1)
                );
                break;
            case R.id.button_moment_add:
                startActivity(AddMomentActivity.newIntent(getActivity(), mUser.getUserID()));
                break;
            case R.id.button_moment_chat:
                startActivity(ChatActivity.newIntent(getActivity(), String.valueOf(mCurrentUser.getUserID()), mCurrentUser.getUserUsername(), String.valueOf(mUser.getUserID()), mUser.getUserUsername()));
                break;
            case R.id.constraintlayout_moment_header:
                getMomentFromServerAndUpdateUI();
                break;
        }
    }

    private void getMomentFromServerAndUpdateUI() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", mUser.getUserID());
        Call call = OkHttpUtils.getInstance().post("getMomentsByID", map);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), R.string.error_internet, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseString = response.body().string();
                JSONObject resultJson = JSON.parseObject(responseString);
                JSONArray jsonArray = resultJson.getJSONArray("moments");
                if (jsonArray.size() > 0) {
                    mMomentList.clear();
                    for (int i = 0; i < jsonArray.size(); i++) {
                        mMomentList.add(jsonArray.getObject(i, Moment.class));
                    }
                    SqliteUtils.updateUserMoments(mSQLiteDatabase, mUser.getUserID(), mMomentList);
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateUI();
                    }
                });
            }
        });
    }

    private void getMomentFromSqliteAndUpdateUI() {
        mMomentList.clear();
        mMomentList.addAll(SqliteUtils.getMomentList(mSQLiteDatabase, mUser.getUserID()));
        updateUI();
    }

    private void updateUI() {
        if (mAdapter == null) {
            mAdapter = new MomentAdapter(mMomentList);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.notifyDataSetChanged();
        }
    }

    private class MomentAdapter extends RecyclerView.Adapter<MomentAdapter.MomentHolder> {

        private List<Moment> mMomentList;

        public MomentAdapter(List<Moment> momentList) {
            mMomentList = momentList;
        }

        @NonNull
        @Override
        public MomentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_moment, parent, false);
            return new MomentHolder(itemView);
        }

        @Override
        public int getItemCount() {
            return mMomentList.size();
        }

        @Override
        public void onBindViewHolder(@NonNull MomentHolder holder, int position) {
            Moment moment = mMomentList.get(position);
            holder.bind(moment, position);
        }

        class MomentHolder extends RecyclerView.ViewHolder {

            private Moment mMoment;
            private int mPosition;

            private TextView mTextViewMomentContent;
            private ImageView mImageViewMomentImage;
            private TextView mTextViewSendDate;

            public MomentHolder(@NonNull View itemView) {
                super(itemView);
                this.mTextViewMomentContent = itemView.findViewById(R.id.textview_moment_item_moment);
                this.mImageViewMomentImage = itemView.findViewById(R.id.imageview_moment_item_image);
                this.mTextViewSendDate = itemView.findViewById(R.id.textview_moment_item_date);
                itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        new MyDialog.Builder(getContext())
                                .setMessage("要删除这条动态吗？")
                                .setPositiveButton(getString(R.string.text_ok), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                    }
                                }).create().show();
                        return true;
                    }
                });
            }

            public void bind(Moment moment, int position) {
                mMoment = moment;
                mPosition = position;
                mTextViewMomentContent.setText(mMoment.getMomentContent());
                if (mMoment.getMomentImage() != null) {
                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(mMoment.getMomentImage());
                    Bitmap bitmap = BitmapFactory.decodeStream(byteArrayInputStream);
                    mImageViewMomentImage.setImageBitmap(bitmap);
                } else {
                    mImageViewMomentImage.setVisibility(View.GONE);
                }
                mTextViewSendDate.setText(MyUtils.dateFormate(getContext(), MyUtils.convertDateToString(mMoment.getSendDate())));
            }
        }
    }
}
