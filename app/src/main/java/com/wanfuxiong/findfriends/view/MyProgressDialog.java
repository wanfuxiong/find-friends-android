package com.wanfuxiong.findfriends.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.wanfuxiong.findfriends.R;

public class MyProgressDialog extends Dialog {

    public MyProgressDialog(@NonNull Context context) {
        super(context);
    }

    public MyProgressDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected MyProgressDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        // ImageView imageView = findViewById(R.id.progress_dialog_my_image);
        // // 获取ImageView上的动画背景
        // AnimationDrawable animationDrawable = (AnimationDrawable) imageView.getBackground();
        // // 开始动画
        // animationDrawable.start();
    }

    public void setTitle(CharSequence title){
        TextView textView = findViewById(R.id.progress_dialog_my_title);
        if (!TextUtils.isEmpty(title)){
            textView.setVisibility(View.VISIBLE);
            textView.setText(title);
            textView.invalidate();
        } else {
            textView.setVisibility(View.INVISIBLE);
        }
    }

    public static MyProgressDialog show(Context context, CharSequence title, boolean cancelable, OnCancelListener cancelListener) {
        MyProgressDialog dialog = new MyProgressDialog(context, R.style.MyProgressDialog);
        dialog.setContentView(R.layout.progress_dialog_my);
        dialog.setTitle(title);
        dialog.setCancelable(cancelable);
        dialog.setOnCancelListener(cancelListener);
        // 设置居中
        dialog.getWindow().getAttributes().gravity = Gravity.CENTER;
        dialog.show();
        return dialog;
    }
}
