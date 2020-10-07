package com.sm.sdk.demo.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;

import androidx.appcompat.widget.AppCompatTextView;

import com.sm.sdk.demo.R;

/**
 * @author Created by Lee64 on 2017/9/7.
 */

public class PasswordEditText extends AppCompatTextView {

    private Paint paint;

    private int textLength = 0;
    private int passwordLength = 12;
    // 缓存输入的密码
    private StringBuilder inputSB;

    public PasswordEditText(Context context) {
        super(context);
        init();
    }

    public PasswordEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PasswordEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.parseColor("#212121"));
        setTextColor(Color.parseColor("#212121"));
        setHintTextColor(Color.parseColor("#b2b2b2"));
        setHint(getResources().getString(R.string.please_pay_password));
        setGravity(Gravity.CENTER);
        setTextSize(18);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // 设置字间距
            setLetterSpacing(0.2f);
        }
        inputSB = new StringBuilder();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        setBackground(null);
        // 画底线
        canvas.drawLine(0, this.getHeight() - 1, this.getWidth() - 1, this.getHeight() - 1, paint);
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        if (inputSB == null) {
            return;
        }
        textLength = text.toString().length();
        invalidate();
    }

    /**
     * 输入一个字符
     */
    public synchronized void addText(String text) {
        if (inputSB.length() == passwordLength) {
            return;
        }
        inputSB.append(text);
        setText(inputSB.toString());
    }

    /**
     * 删除所有
     */
    public void clearText() {
        if (inputSB.length() == 0) {
            return;
        }
        inputSB.delete(0, inputSB.length());
        setText(inputSB.toString());
    }

    /**
     * 删除最后一个密码
     */
    public void delLast() {
        if (inputSB.length() == 0)
            return;
        inputSB.deleteCharAt(inputSB.length() - 1);
        setText(inputSB.toString());
    }

    public String getPassword() {
        if (inputSB == null) {
            return null;
        }
        return inputSB.toString();
    }

}
