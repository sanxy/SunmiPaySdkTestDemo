package com.sm.sdk.demo.view;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;

import androidx.annotation.NonNull;

import com.sm.sdk.demo.R;

public class SwingCardHintDialog extends Dialog {

    public SwingCardHintDialog(@NonNull Context context) {
        this(context, R.style.DefaultDialogStyle);
    }

    public SwingCardHintDialog(@NonNull Context context, int theme) {
        super(context, theme);
        init();
    }

    private void init() {
        setContentView(R.layout.dialog_nfc_hint);
        if (getWindow() != null) {
            // 居中
            getWindow().getAttributes().gravity = Gravity.CENTER;
        }
        // 点击空白不取消
        setCanceledOnTouchOutside(false);
        // 点击返回按钮不取消
        setCancelable(true);
    }


}
