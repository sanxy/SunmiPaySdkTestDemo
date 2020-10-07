package com.sm.sdk.demo.etc;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.R;

public class ETCActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((R.layout.activity_etc));
        initToolbarBringBack(R.string.etc);
        initView();
    }

    private void initView() {
        View view = findViewById(R.id.search_device);
        TextView leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.etc_search_etc_device);

        view = findViewById(R.id.i2c_data_exchange);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.etc_i2c_data_exchange);

        view = findViewById(R.id.stress_test);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.etc_stress_test);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.search_device:
                openActivity(SearchETCDeviceActivity.class);
                break;
            case R.id.i2c_data_exchange:
                openActivity(I2CDataExchangeActivity.class);
                break;
            case R.id.stress_test:
                openActivity(ETCStressTestActivity.class);
                break;
        }
    }
}
