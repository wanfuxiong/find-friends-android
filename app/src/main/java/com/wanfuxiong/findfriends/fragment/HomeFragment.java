package com.wanfuxiong.findfriends.fragment;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wanfuxiong.findfriends.R;
import com.wanfuxiong.findfriends.activity.InformationActivity;
import com.wanfuxiong.findfriends.activity.MainActivity;
import com.wanfuxiong.findfriends.activity.MatchActivity;
import com.wanfuxiong.findfriends.activity.MomentActivity;
import com.wanfuxiong.findfriends.pojo.Message;
import com.wanfuxiong.findfriends.pojo.User;
import com.wanfuxiong.findfriends.util.MyUtils;
import com.wanfuxiong.findfriends.util.OkHttpUtils;
import com.wanfuxiong.findfriends.util.SqliteUtils;
import com.wanfuxiong.findfriends.util.UIUtils;
import com.wanfuxiong.soulplanet.adapter.PlanetAdapter;
import com.wanfuxiong.soulplanet.utils.SizeUtils;
import com.wanfuxiong.soulplanet.view.PlanetView;
import com.wanfuxiong.soulplanet.view.SoulPlanetsView;

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

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private static final String EXTRA_CURRENT_USER_ID = "current_user_id";
    private static final String EXTRA_CURRENT_USER_USERNAME = "current_user_username";
    private static final String EXTRA_CURRENT_USER_INTEREST = "current_user_interest";

    private SoulPlanetsView mSoulPlanetsView;
    private LinearLayout mLinearLayout1;
    private LinearLayout mLinearLayout2;
    private LinearLayout mLinearLayout3;
    private TextView mTextViewAmount;

    private int amount;// 当前在线用户总数

    private User currentUser;
    private int currentUserID;
    private String currentUserUsername;
    private String currentUserInterest;

    private SQLiteDatabase mSQLiteDatabase;

    private MyPlanetAdapter mAdapter;
    private List<User> mUserList;

    // private MediaPlayer mediaPlayer;

    @SuppressLint("SetTextI18n")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        root.findViewById(R.id.view_home).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "正在刷新星球数据……", Toast.LENGTH_SHORT).show();
                getSimpleUserListFromServerAndUpdateUI();
            }
        });

        mSoulPlanetsView = root.findViewById(R.id.planetsview_home);
        mSoulPlanetsView.setOnTagClickListener(new SoulPlanetsView.OnTagClickListener() {
            @Override
            public void onItemClick(ViewGroup parent, View view, int position) {
                startActivity(MomentActivity.newIntent(getContext(), currentUserID,mUserList.get(position).getUserID()));
            }
        });

        mTextViewAmount = root.findViewById(R.id.textview_home_amount);

        mLinearLayout1 = root.findViewById(R.id.ll_home1);
        mLinearLayout2 = root.findViewById(R.id.ll_home2);
        mLinearLayout3 = root.findViewById(R.id.ll_home3);
        mLinearLayout1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MatchActivity.newIntent(getContext(),currentUserID,currentUserUsername,1,currentUserInterest)));
            }
        });
        mLinearLayout2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MatchActivity.newIntent(getContext(),currentUserID,currentUserUsername,2,currentUserInterest)));
            }
        });
        mLinearLayout3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MatchActivity.newIntent(getContext(),currentUserID,currentUserUsername,0,currentUserInterest)));
            }
        });
        return root;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        currentUserID = getActivity().getIntent().getIntExtra(EXTRA_CURRENT_USER_ID, 0);
        currentUserUsername = getActivity().getIntent().getStringExtra(EXTRA_CURRENT_USER_USERNAME);
        currentUserInterest = getActivity().getIntent().getStringExtra(EXTRA_CURRENT_USER_INTEREST);

        mUserList = new ArrayList<>();

        // 创建或打开数据库
        mSQLiteDatabase = getActivity().openOrCreateDatabase("ff" + currentUserID + ".db", Context.MODE_PRIVATE, null);
        currentUser = SqliteUtils.getCurrentUserInfo(mSQLiteDatabase);
        currentUserUsername = currentUser.getUserUsername();

        Log.d(TAG,"注册EventBus事件");
        EventBus.getDefault().register(this);
        // mediaPlayer = MediaPlayer.create(getActivity(), R.raw.home);
        // mediaPlayer.start();
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        if (mSQLiteDatabase.query("user", null, null, null, null, null, null).getCount() > 0) {
            getSimpleUserListFromSqliteAndUpdateUI();
        } else {
            Toast.makeText(getContext(), "正在刷新星球数据……", Toast.LENGTH_SHORT).show();
            getSimpleUserListFromServerAndUpdateUI();
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        // UIUtils.setStatusBarColor(getActivity(), ContextCompat.getColor(getActivity(), R.color.colorPlanetBackground));
        UIUtils.setAndroidNativeLightStatusBar(getActivity(), false);
        mTextViewAmount.setText(getString(R.string.text_user_count) + ": ");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        // mediaPlayer.pause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        EventBus.getDefault().unregister(this);
    }

    @SuppressLint("SetTextI18n")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateMsgView(Message message) {
        Log.d(TAG,"EventBus");
        if (message.getFromUserID() == 0) {
            amount = Integer.parseInt(message.getMessageData());
            mTextViewAmount.setText(getString(R.string.text_user_count) + ": " + amount);
            Log.d(TAG,"EventBus 更新在线用户数量："+amount);
        }
    }

    private void getSimpleUserListFromSqliteAndUpdateUI() {
        // 从sqlite中取得兴趣相同的伙伴，赋值给mUserList在home页面展示
        Cursor cursor = mSQLiteDatabase.query("home", null, null, null, null, null, null);
        cursor.moveToFirst();
        mUserList.clear();
        for (int i = 0; i < cursor.getCount(); i++) {
            mUserList.add(new User(
                    cursor.getInt(0),
                    cursor.getString(1),
                    null,
                    null,
                    cursor.getInt(3),
                    cursor.getString(4),
                    null,
                    null,
                    null,
                    new Date(cursor.getString(7))
            ));
            cursor.moveToNext();
        }

        Log.d(TAG, "星球从sqlite中加载的用户个数：" + mUserList.size());

        cursor.close();

        updateUI();
    }

    private void getSimpleUserListFromServerAndUpdateUI() {
        Map<String, Object> map = new HashMap<>();
        Call call = OkHttpUtils.getInstance().post("getSimpleUserList", map);

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
                JSONArray jsonArray = resultJson.getJSONArray("users");
                // 清楚home表数据，放入最新数据，并更新user表用户数据
                mSQLiteDatabase.execSQL("DELETE FROM `home`");
                for (int i = 0; i < jsonArray.size(); i++) {
                    User user = jsonArray.getObject(i, User.class);
                    SqliteUtils.insertOrUpdateUserInfo(mSQLiteDatabase, user);
                    ContentValues cv = new ContentValues();
                    cv.put("user_id", user.getUserID());
                    cv.put("user_username", user.getUserUsername());
                    cv.put("user_signature", user.getUserSignature());
                    cv.put("user_sex", user.getUserSex());
                    cv.put("user_interest", user.getUserInterest());
                    cv.put("user_phone_number", user.getUserPhoneNumber());
                    cv.put("user_qq", user.getUserQQ());
                    cv.put("create_date", user.getCreateDate().toString());
                    mSQLiteDatabase.insert("home", null, cv);
                }
                Log.d(TAG, "星球列表最新数据已经更新并写入sqlite");
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getSimpleUserListFromSqliteAndUpdateUI();
                    }
                });
            }
        });
    }

    private void updateUI() {
        if (mAdapter == null) {
            mAdapter = new MyPlanetAdapter(mUserList);
            mSoulPlanetsView.setAdapter(mAdapter);
        } else {
            mAdapter.notifyDataSetChanged();
        }
    }

    public class MyPlanetAdapter extends PlanetAdapter {

        private List<User> mUsers;

        public MyPlanetAdapter(List<User> users) {
            mUsers = users;
        }

        @Override
        public int getCount() {
            return 50;
        }

        @Override
        public View getView(Context context, int position, ViewGroup parent) {
            User user = mUsers.get(position);
            PlanetView planetView = new PlanetView(context);
            planetView.setSign(user.getUserUsername());

            int starColor = 0;
            boolean hasShadow = false;
            String str = "";
            String degree = "";
            if (user.getUserSex() == 0) {
                starColor = PlanetView.COLOR_FEMALE;
            } else if (user.getUserSex() == 1) {
                starColor = PlanetView.COLOR_MALE;
            }

            if (!user.getUserInterest().equals("") && !user.getUserSex().equals(currentUser.getUserSex()) && user.getUserInterest().equals(currentUserInterest)) {
                starColor = PlanetView.COLOR_BEST_MATCH;
                hasShadow = true;
                str = "最匹配";
                degree = "100%";
            } else if (!user.getUserInterest().equals("")) {
                str = "";
                double similarity = MyUtils.calculateSimilarity(user.getUserInterest(), currentUserInterest);
                degree = similarity == 0.0 ? ("") : (similarity + "%");
            } else {
                str = "";
                degree = "";
            }
            planetView.setStarColor(starColor);
            planetView.setHasShadow(hasShadow);
            planetView.setMatch(degree, str);

            if (hasShadow) {
                planetView.setMatchColor(starColor);
            } else {
                planetView.setMatchColor(PlanetView.COLOR_MOST_ACTIVE);
            }
            int starWidth = SizeUtils.dp2px(context, 50.0f);
            int starHeight = SizeUtils.dp2px(context, 85.0f);
            int starPaddingTop = SizeUtils.dp2px(context, 20.0f);
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(starWidth, starHeight);
            planetView.setPadding(0, starPaddingTop, 0, 0);
            planetView.setLayoutParams(layoutParams);
            return planetView;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public int getPopularity(int position) {
            return position % 10;
        }

        @Override
        public void onThemeColorChanged(View view, int themeColor) {

        }
    }
}
