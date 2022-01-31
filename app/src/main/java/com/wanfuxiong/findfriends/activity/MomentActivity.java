package com.wanfuxiong.findfriends.activity;

import android.content.Context;
import android.content.Intent;

import androidx.fragment.app.Fragment;

import com.wanfuxiong.findfriends.fragment.MomentFragment;

public class MomentActivity extends SingleFragmentActivity {

    private final static String TAG = "MomentActivity";
    private final static String EXTRA_CURRENT_USER_ID = "current_user_id";
    private final static String EXTRA_USER_ID = "user_id";

    public static Intent newIntent(Context context, int currentUserID, int userID) {
        Intent intent = new Intent(context, MomentActivity.class);
        intent.putExtra(EXTRA_CURRENT_USER_ID, currentUserID);
        intent.putExtra(EXTRA_USER_ID, userID);
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        // return MomentFragment.newInstance(
        //         getIntent().getIntExtra(EXTRA_CURRENT_USER_ID, 0),
        //         getIntent().getIntExtra(EXTRA_USER_ID, 0)
        // );
        return new MomentFragment();
    }
}