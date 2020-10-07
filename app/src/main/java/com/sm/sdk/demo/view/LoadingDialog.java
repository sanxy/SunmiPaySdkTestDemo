package com.sm.sdk.demo.view;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.Window;
import android.widget.TextView;

import com.sm.sdk.demo.R;

public class LoadingDialog extends Dialog {

    private TextView mTvMessage;

    public LoadingDialog(Context context, String text) {
        this(context, R.style.DefaultDialogStyle, text);
    }

    private LoadingDialog(Context context, int theme, String text) {
        super(context, theme);
        init(text);
    }

    private void init(String msg) {
        setContentView(R.layout.dialog_loading);
        Window window = getWindow();
        if (window != null) {
            window.getAttributes().gravity = Gravity.CENTER;
        }
        setCanceledOnTouchOutside(false);
        setCancelable(false);

        mTvMessage = findViewById(R.id.tv_message);
        if (msg == null || msg.trim().length() == 0) {
            mTvMessage.setText(R.string.loading);
        } else {
            mTvMessage.setText(msg);
        }
    }

    public void setMessage(String msg) {
        mTvMessage.setText(msg);
    }

}
