package com.wanfuxiong.findfriends.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.wanfuxiong.findfriends.R;

public class ChangePasswordDialog extends Dialog implements DialogInterface {

    public ChangePasswordDialog(@NonNull Context context) {
        super(context);
    }

    public ChangePasswordDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        this.getWindow().setBackgroundDrawableResource(R.drawable.background_dialog);
    }

    protected ChangePasswordDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public static class Builder {

        private View mLayout;

        private EditText mEditTextOriginal;
        private EditText mEditTextNew;
        private EditText mEditTextAgain;
        private MaterialButton mButtonPositive;
        private MaterialButton mButtonNegative;

        private ChangePasswordDialog mDialog;

        public Builder(Context context) {
            mDialog = new ChangePasswordDialog(context, R.style.Theme_AppCompat_Dialog);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //加载布局文件
            mLayout = inflater.inflate(R.layout.dialog_change_password, null, false);
            //添加布局文件到 Dialog
            mDialog.addContentView(mLayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            mEditTextOriginal = mLayout.findViewById(R.id.dialog_change_password_original);
            mEditTextNew = mLayout.findViewById(R.id.dialog_change_password_new);
            mEditTextAgain = mLayout.findViewById(R.id.dialog_change_password_again);
            mButtonPositive = mLayout.findViewById(R.id.dialog_change_password_ok);
            mButtonNegative = mLayout.findViewById(R.id.dialog_change_password_cancel);
        }

        public EditText getEditTextOriginal(){
            return mEditTextOriginal;
        }

        public EditText getEditTextNew(){
            return mEditTextNew;
        }

        public EditText getEditTextAgain(){
            return mEditTextAgain;
        }

        public String getEditTextOriginalText(){
            return mEditTextOriginal.getText().toString().trim();
        }

        public String getEditTextNewText(){
            return mEditTextNew.getText().toString().trim();
        }

        public String getEditTextAgainText(){
            return mEditTextAgain.getText().toString().trim();
        }

        public MaterialButton getButtonPositive(){
            return mButtonPositive;
        }

        public ChangePasswordDialog create() {
            mEditTextOriginal.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // 禁止EditText输入空格
                    if (s.toString().contains(" ")) {
                        String[] str = s.toString().split(" ");
                        StringBuilder sb = new StringBuilder();
                        for (String value : str) {
                            sb.append(value);
                        }
                        mEditTextOriginal.setText(sb.toString());
                        mEditTextOriginal.setSelection(start);
                    }
                    Log.d("editTextOriginal", mEditTextOriginal.getText().toString());
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
            mEditTextNew.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // 禁止EditText输入空格
                    if (s.toString().contains(" ")) {
                        String[] str = s.toString().split(" ");
                        StringBuilder sb = new StringBuilder();
                        for (String value : str) {
                            sb.append(value);
                        }
                        mEditTextNew.setText(sb.toString());
                        mEditTextNew.setSelection(start);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
            mEditTextAgain.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // 禁止EditText输入空格
                    if (s.toString().contains(" ")) {
                        String[] str = s.toString().split(" ");
                        StringBuilder sb = new StringBuilder();
                        for (String value : str) {
                            sb.append(value);
                        }
                        mEditTextAgain.setText(sb.toString());
                        mEditTextAgain.setSelection(start);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
            mButtonNegative.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mDialog.dismiss();
                }
            });
            mDialog.setContentView(mLayout);
            mDialog.setCancelable(true);                //用户可以点击后退键关闭 Dialog
            mDialog.setCanceledOnTouchOutside(false);   //用户不可以点击外部来关闭 Dialog
            return mDialog;
        }
    }
}
