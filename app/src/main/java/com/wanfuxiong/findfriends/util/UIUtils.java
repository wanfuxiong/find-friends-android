package com.wanfuxiong.findfriends.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.wanfuxiong.findfriends.R;
import com.wanfuxiong.findfriends.pojo.User;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Date;

public class UIUtils {
    public static void setProfile(User user, ImageView imageView) {
        if (user.getUserProfile() == null && (user.getUserSex() == 1 || user.getUserSex() == null)) {
            imageView.setImageResource(R.drawable.profile_male_default);
        } else if (user.getUserProfile() == null && user.getUserSex() == 0) {
            imageView.setImageResource(R.drawable.profile_female_default);
        } else {
            // try {
            //     InputStream inputStream = user.getUserProfile().getBinaryStream();
            //     Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            //     imageView.setImageBitmap(bitmap);
            // } catch (SQLException e) {
            //     e.printStackTrace();
            // }
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(user.getUserProfile());
            Bitmap bitmap = BitmapFactory.decodeStream(byteArrayInputStream);
            imageView.setImageBitmap(bitmap);
        }
    }

    // 设置状态栏背景色
    public static void setStatusBarColor(Activity activity, int colorId) {
        Window window = activity.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(colorId);
    }

    // 设置状态栏背景色2
    public static void setStatusBarBackground(Activity activity, int resId) {
        Drawable background = activity.getDrawable(resId);
        Window window = activity.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setBackgroundDrawable(background);
    }

    // 设置状态栏文字颜色
    public static void setAndroidNativeLightStatusBar(Activity activity, boolean dark) {
        View decor = activity.getWindow().getDecorView();
        if (dark) {
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }

    @SuppressLint("RestrictedApi")
    public static void disableShiftMode(BottomNavigationView navigationView) {
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) navigationView.getChildAt(0);
        try {
            Field shiftingMode = menuView.getClass().getDeclaredField("mShiftingMode");
            shiftingMode.setAccessible(true);
            shiftingMode.setBoolean(menuView, false);
            shiftingMode.setAccessible(false);

            for (int i = 0; i < menuView.getChildCount(); i++) {
                BottomNavigationItemView itemView = (BottomNavigationItemView) menuView.getChildAt(i);
                itemView.setShifting(false);
                itemView.setChecked(itemView.getItemData().isChecked());
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    // 获取状态栏高度
    public static int getStatusBarHeight(Context context) {
        int statusBarHeight = -1;
        //获取status_bar_height资源的ID
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            //根据资源ID获取响应的尺寸值
            statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        }
        return statusBarHeight;
    }

    public static void setItemDialog(AlertDialog dialog, int width) {
        Window window = dialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = width;
        lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
        window.setBackgroundDrawableResource(R.drawable.background_dialog);
    }

    // dp转像素
    public static int getPixelsFromDp(int i) {
        DisplayMetrics metrics = new DisplayMetrics();
        // getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return (i * metrics.densityDpi) / DisplayMetrics.DENSITY_DEFAULT;
    }
}
