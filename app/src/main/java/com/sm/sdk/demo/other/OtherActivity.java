package com.sm.sdk.demo.other;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.R;

public class OtherActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other);
        initToolbarBringBack(R.string.other);
        initView();
    }

    private void initView() {
        View item = findViewById(R.id.other_language);
        TextView leftText = item.findViewById(R.id.left_text);
        leftText.setText(R.string.other_language);
        item.setOnClickListener(this);

        item = findViewById(R.id.other_e_signature);
        leftText = item.findViewById(R.id.left_text);
        leftText.setText(R.string.e_signature);
        item.setOnClickListener(this);

        item = findViewById(R.id.other_version);
        leftText = item.findViewById(R.id.left_text);
        leftText.setText(R.string.version);
        item.setOnClickListener(this);

        item = findViewById(R.id.other_multi_thread);
        leftText = item.findViewById(R.id.left_text);
        leftText.setText(R.string.multi_thread_send_package);
        item.setOnClickListener(this);

        item = findViewById(R.id.other_tamper_log);
        leftText = item.findViewById(R.id.left_text);
        leftText.setText(R.string.tamper_log);
        item.setOnClickListener(this);

        item = findViewById(R.id.other_screen);
        leftText = item.findViewById(R.id.left_text);
        leftText.setText(R.string.screen_rotation);
        item.setOnClickListener(this);

        item = findViewById(R.id.other_gb2312);
        leftText = item.findViewById(R.id.left_text);
        leftText.setText("GB2312");
        item.setOnClickListener(this);

        item = findViewById(R.id.other_date_time);
        leftText = item.findViewById(R.id.left_text);
        leftText.setText(R.string.set_date_time);
        item.setOnClickListener(this);

        item = findViewById(R.id.other_setting);
        leftText = item.findViewById(R.id.left_text);
        leftText.setText(R.string.setting);
        item.setOnClickListener(this);

        item = findViewById(R.id.other_device_comm);
        leftText = item.findViewById(R.id.left_text);
        leftText.setText(R.string.device_comm);
        item.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.other_language:
                openActivity(LanguageActivity.class);
                break;
            case R.id.other_e_signature:
                openActivity(ESignatureActivity.class);
                break;
            case R.id.other_version:
                openActivity(VersionActivity.class);
                break;
            case R.id.other_multi_thread:
                openActivity(MultiThreadTestActivity.class);
                break;
            case R.id.other_tamper_log:
                openActivity(TamperLogActivity.class);
                break;
            case R.id.other_screen:
                openActivity(ScreenActivity.class);
                break;
            case R.id.other_gb2312:
                openActivity(GB2312Activity.class);
                break;
            case R.id.other_date_time:
                openActivity(SetTimeDateActivity.class);
                break;
            case R.id.other_setting:
                openActivity(SettingActivity.class);
                break;
            case R.id.other_device_comm:
                openActivity(DeviceCommActivity.class);
                break;
        }
    }


}
