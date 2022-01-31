package com.wanfuxiong.findfriends.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.wanfuxiong.findfriends.R;

public class MyDialog extends Dialog implements DialogInterface {

    public MyDialog(@NonNull Context context) {
        super(context);
    }

    public MyDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        this.getWindow().setBackgroundDrawableResource(R.drawable.background_dialog);
    }

    protected MyDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public static class Builder {

        private View mLayout;

        private ImageView mIcon;
        private TextView mTitle;
        private TextView mMessage;
        private MaterialButton mButtonPositive;
        private MaterialButton mButtonNegative;
        private ImageButton mButtonNeutral;

        private View.OnClickListener mButtonPositiveClickListener;
        private View.OnClickListener mButtonNegativeClickListener;
        private View.OnClickListener mButtonNeutralClickListener;

        private MyDialog mDialog;

        public Builder(Context context) {
            mDialog = new MyDialog(context, R.style.Theme_AppCompat_Dialog);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //加载布局文件
            mLayout = inflater.inflate(R.layout.dialog_my, null, false);
            //添加布局文件到 Dialog
            mDialog.addContentView(mLayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            mIcon = mLayout.findViewById(R.id.dialog_my_icon);
            mTitle = mLayout.findViewById(R.id.dialog_my_title);
            mMessage = mLayout.findViewById(R.id.dialog_my_message);
            mButtonPositive = mLayout.findViewById(R.id.dialog_my_positive_button);
            mButtonNegative = mLayout.findViewById(R.id.dialog_my_negative_button);
            mButtonNeutral = mLayout.findViewById(R.id.dialog_my_neutral_button);
        }

        /**
         * 通过 ID 设置 Dialog 图标
         */
        public Builder setIcon(int resId) {
            mIcon.setImageResource(resId);
            return this;
        }

        /**
         * 用 Bitmap 作为 Dialog 图标
         */
        public Builder setIcon(Bitmap bitmap) {
            mIcon.setImageBitmap(bitmap);
            return this;
        }

        /**
         * 设置 Dialog 标题
         */
        public Builder setTitle(@NonNull String title) {
            mTitle.setText(title);
            mTitle.setVisibility(View.VISIBLE);
            return this;
        }

        /**
         * 设置 Message
         */
        public Builder setMessage(@NonNull String message) {
            mMessage.setText(message);
            return this;
        }

        /**
         * 设置确定按钮文字和监听
         */
        public Builder setPositiveButton(@NonNull String text, View.OnClickListener listener) {
            mButtonPositive.setText(text);
            mButtonPositiveClickListener = listener;
            return this;
        }

        /**
         * 设置否定按钮文字和监听
         */
        public Builder setNegativeButton(@NonNull String text, View.OnClickListener listener) {
            mButtonNegative.setText(text);
            mButtonNegativeClickListener = listener;
            mButtonNegative.setVisibility(View.VISIBLE);
            return this;
        }

        /**
         * 设置中立按钮文字和监听
         */
        public Builder setNeutralButton(View.OnClickListener listener) {
            mButtonNeutralClickListener = listener;
            mButtonNeutral.setVisibility(View.VISIBLE);
            return this;
        }

        public MaterialButton getButtonNegative(){
            return mButtonNegative;
        }

        public MyDialog create() {
            mButtonPositive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mDialog.dismiss();
                    mButtonPositiveClickListener.onClick(view);
                }
            });
            mButtonNegative.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mDialog.dismiss();
                    mButtonNegativeClickListener.onClick(view);
                }
            });
            mButtonNeutral.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mDialog.dismiss();
                    mButtonNeutralClickListener.onClick(view);
                }
            });
            mDialog.setContentView(mLayout);
            mDialog.setCancelable(true);                //用户可以点击后退键关闭 Dialog
            mDialog.setCanceledOnTouchOutside(false);   //用户不可以点击外部来关闭 Dialog
            return mDialog;
        }
    }
}
