package com.wanfuxiong.findfriends.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.wanfuxiong.findfriends.R;
import com.wanfuxiong.findfriends.activity.AboutActivity;
import com.wanfuxiong.findfriends.activity.InformationActivity;
import com.wanfuxiong.findfriends.activity.LoginActivity;
import com.wanfuxiong.findfriends.activity.SettingsActivity;
import com.wanfuxiong.findfriends.pojo.Moment;
import com.wanfuxiong.findfriends.pojo.User;
import com.wanfuxiong.findfriends.util.MyUtils;
import com.wanfuxiong.findfriends.util.SqliteUtils;
import com.wanfuxiong.findfriends.util.UIUtils;
import com.wanfuxiong.findfriends.view.MyDialog;
import com.wanfuxiong.findfriends.view.MyProgressDialog;

import java.util.List;

import lombok.SneakyThrows;

public class MeFragment extends Fragment implements View.OnClickListener {

    private static final String EXTRA_USER_ID = "user_id";

    private TextView mTextViewUsername;
    private TextView mTextViewID;
    private ImageView mImageViewUserProfile;
    private View mViewUpdate;
    private View mViewSettings;
    private View mViewAbout;
    private View mViewLogout;
    private View mViewInformation;

    private SQLiteDatabase mSQLiteDatabase;

    private User currentUser;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("MeFragment", "onCreateView");
        View root = inflater.inflate(R.layout.fragment_me, container, false);
        root.setPadding(0, UIUtils.getStatusBarHeight(getContext()), 0, 0);

        mViewInformation = root.findViewById(R.id.constraintlayout_information);
        mTextViewUsername = root.findViewById(R.id.textview_me_username);
        mTextViewID = root.findViewById(R.id.textview_me_id);
        mImageViewUserProfile = root.findViewById(R.id.imageview_me_user_profile);
        mViewLogout = root.findViewById(R.id.view_me_logout);
        mViewAbout = root.findViewById(R.id.view_me_about);
        mViewSettings = root.findViewById(R.id.view_me_settings);
        mViewUpdate = root.findViewById(R.id.view_me_update);

        mViewUpdate.setOnClickListener(this);
        mViewSettings.setOnClickListener(this);
        mViewLogout.setOnClickListener(this);
        mViewAbout.setOnClickListener(this);
        mViewInformation.setOnClickListener(this);
        return root;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("MeFragment", "onCreate");

        int currentUserID = getActivity().getIntent().getIntExtra(EXTRA_USER_ID, 0);

        // 创建或打开数据库
        mSQLiteDatabase = getActivity().openOrCreateDatabase("ff" + currentUserID + ".db", Context.MODE_PRIVATE, null);

    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("MeFragment", "onStart");

        MyUtils.updateResources(getContext());

        currentUser = SqliteUtils.getCurrentUserInfo(mSQLiteDatabase);

        mTextViewUsername.setText(currentUser.getUserUsername());
        mTextViewID.setText(String.valueOf(currentUser.getUserID()));
        UIUtils.setProfile(currentUser, mImageViewUserProfile);

        // UIUtils.setStatusBarColor(getActivity(), ContextCompat.getColor(getActivity(),R.color.colorAppBackground));
        UIUtils.setAndroidNativeLightStatusBar(getActivity(), true);

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.constraintlayout_information:
                Intent infoIntent = InformationActivity.newIntent(getActivity(), currentUser.getUserID().toString(), currentUser.getUserID().toString(), 0);
                getActivity().startActivity(infoIntent);
                break;
            case R.id.view_me_logout:
                new MyDialog.Builder(getActivity())
                        .setIcon(R.drawable.ic_dialog_beg)
                        .setMessage("确定要退出登录吗")
                        .setPositiveButton(getString(R.string.text_ok), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(getActivity(), LoginActivity.class);
                                startActivity(intent);
                                getActivity().overridePendingTransition(0, 0);
                                getActivity().finish();
                            }
                        })
                        .setNegativeButton(getString(R.string.text_cancel), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        }).create().show();
                break;
            case R.id.view_me_about:
                Intent aboutIntent = new Intent(getActivity(), AboutActivity.class);
                getActivity().startActivity(aboutIntent);
                break;
            case R.id.view_me_settings:
                getActivity().startActivity(SettingsActivity.newIntent(getActivity(), currentUser.getUserID()));
                break;
            case R.id.view_me_update:
                final MyProgressDialog progressDialog = MyProgressDialog.show(getContext(),getString(R.string.text_loading_update),false,null);
                new Thread(new Runnable() {
                    @SneakyThrows
                    @Override
                    public void run() {
                        Thread.sleep(3000);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                                new MyDialog.Builder(getActivity())
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
        }
    }
}
