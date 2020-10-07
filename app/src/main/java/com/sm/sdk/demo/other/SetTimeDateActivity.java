package com.sm.sdk.demo.other;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.LogUtil;
import com.sm.sdk.demo.utils.SystemDateTime;

import java.util.Calendar;

public class SetTimeDateActivity extends BaseAppCompatActivity {

    private EditText etYear, etMonth, etDay, etHour, etMinute, etSecond;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_time);
        initToolbarBringBack(R.string.set_date_time);
        initView();
        fillDefDateTime();
    }

    @SuppressLint("SetTextI18n")
    private void fillDefDateTime() {
        Calendar c = Calendar.getInstance();
        etYear.setText(c.get(Calendar.YEAR) + "");
        etMonth.setText(c.get(Calendar.MONTH) + 1 + "");
        etDay.setText(c.get(Calendar.DAY_OF_MONTH) + "");
        etHour.setText(c.get(Calendar.HOUR_OF_DAY) + "");
        etMinute.setText(c.get(Calendar.MINUTE) + "");
        etSecond.setText(c.get(Calendar.SECOND) + "");
    }

    private void initView() {
        etYear = findViewById(R.id.et_year);
        etMonth = findViewById(R.id.et_month);
        etDay = findViewById(R.id.et_day);
        etHour = findViewById(R.id.et_hour);
        etMinute = findViewById(R.id.et_minute);
        etSecond = findViewById(R.id.et_second);
        findViewById(R.id.ok).setOnClickListener((v) -> ok(v));
    }

    private void setDateTime(int year, int month, int day, int hour, int minute, int second) throws Exception {
        SystemDateTime.setSysDate(year, month - 1, day, this);
        SystemDateTime.setSysTime(hour, minute, second, this);
    }

    public void ok(View view) {
        try {
            setDateTime(
                    Integer.parseInt(etYear.getText().toString()),
                    Integer.parseInt(etMonth.getText().toString()),
                    Integer.parseInt(etDay.getText().toString()),
                    Integer.parseInt(etHour.getText().toString()),
                    Integer.parseInt(etMinute.getText().toString()),
                    Integer.parseInt(etSecond.getText().toString())
            );
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e("lxy", "e:" + e.getMessage());
        }

    }
}
