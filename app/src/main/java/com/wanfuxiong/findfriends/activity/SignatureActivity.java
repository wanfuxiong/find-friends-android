package com.wanfuxiong.findfriends.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toolbar;

import com.wanfuxiong.findfriends.R;
import com.wanfuxiong.findfriends.util.UIUtils;

public class SignatureActivity extends AppCompatActivity {

    public static final String EXTRA_USER_SIGNATURE = "user_signature";

    private Toolbar mToolbar;
    private EditText mEditText;
    private TextView mTextView;

    private static String signature;

    public static Intent newIntent(Context context, String signature) {
        Intent intent = new Intent(context, SignatureActivity.class);
        intent.putExtra(EXTRA_USER_SIGNATURE, signature);
        return intent;
    }

    public static String getSignatureFromExtra(Intent result) {
        return result.getStringExtra(EXTRA_USER_SIGNATURE);
    }

    private void setSignatureResult (String signature) {
        Intent data = new Intent();
        data.putExtra(EXTRA_USER_SIGNATURE, signature);
        setResult(RESULT_OK, data);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signature);

        mToolbar = findViewById(R.id.toolbar_signature);
        mEditText = findViewById(R.id.edittext_signature_signature);
        mTextView = findViewById(R.id.textview_signature_words);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        signature = getIntent().getStringExtra(EXTRA_USER_SIGNATURE);
        mEditText.setText(signature);
        mTextView.setText(signature.length() + "/20");
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mEditText.getText().length() > 20) {
                    mTextView.setText("字数超过20！ " + mEditText.getText().length() + "/20");
                    mTextView.setTextColor(Color.RED);
                } else {
                    mTextView.setText(mEditText.getText().length() + "/20");
                    mTextView.setTextColor(getResources().getColor(R.color.colorMyGray));
                    setSignatureResult(mEditText.getText().toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
}