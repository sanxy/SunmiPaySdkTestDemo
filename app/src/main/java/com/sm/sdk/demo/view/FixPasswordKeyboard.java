package com.sm.sdk.demo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sm.sdk.demo.R;

public class FixPasswordKeyboard extends LinearLayout {

    public FixPasswordKeyboard(Context context) {
        this(context, null);
    }

    public FixPasswordKeyboard(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FixPasswordKeyboard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private TextView key_0, key_1, key_2;
    private TextView key_3, key_4, key_5;
    private TextView key_6, key_7, key_8;
    private TextView key_9;

    private void initView(Context context) {
        inflate(context, R.layout.view_fix_password_keyboard, this);
        key_0 = findViewById(R.id.text_0);
        key_1 = findViewById(R.id.text_1);
        key_2 = findViewById(R.id.text_2);
        key_3 = findViewById(R.id.text_3);
        key_4 = findViewById(R.id.text_4);
        key_5 = findViewById(R.id.text_5);
        key_6 = findViewById(R.id.text_6);
        key_7 = findViewById(R.id.text_7);
        key_8 = findViewById(R.id.text_8);
        key_9 = findViewById(R.id.text_9);
    }

    public void setKeyBoard(String keys) {
        if (keys == null || keys.length() != 10) return;

        String temp = keys.substring(0, 1);
        key_0.setText(temp);

        temp = keys.substring(1, 2);
        key_1.setText(temp);

        temp = keys.substring(2, 3);
        key_2.setText(temp);

        temp = keys.substring(3, 4);
        key_3.setText(temp);

        temp = keys.substring(4, 5);
        key_4.setText(temp);

        temp = keys.substring(5, 6);
        key_5.setText(temp);

        temp = keys.substring(6, 7);
        key_6.setText(temp);

        temp = keys.substring(7, 8);
        key_7.setText(temp);

        temp = keys.substring(8, 9);
        key_8.setText(temp);

        temp = keys.substring(9, 10);
        key_9.setText(temp);
    }

    public TextView getKey_0() {
        return key_0;
    }

}
