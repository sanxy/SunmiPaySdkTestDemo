package com.sm.sdk.demo.emv;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.R;

public class EMVActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emv);
        initToolbarBringBack(R.string.emv);
        initView();
    }

    private void initView() {
        View item = findViewById(R.id.item_ic);
        TextView leftText = item.findViewById(R.id.left_text);
        leftText.setText(R.string.emv_ic_process);
        item.setOnClickListener(this);

        item = findViewById(R.id.item_mag);
        leftText = item.findViewById(R.id.left_text);
        leftText.setText(R.string.emv_mag_process);
        item.setOnClickListener(this);

        item = findViewById(R.id.item_rupay);
        leftText = item.findViewById(R.id.left_text);
        leftText.setText(R.string.emv_read_rupay_card);
        item.setOnClickListener(this);

        item = findViewById(R.id.item_other);
        leftText = item.findViewById(R.id.left_text);
        leftText.setText(R.string.emv_other);
        item.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.item_ic:
                openActivity(ICProcessActivity.class);
                break;
            case R.id.item_mag:
                openActivity(MagProcessActivity.class);
                break;
            case R.id.item_rupay:
                openActivity(RuPayCardActivity.class);
                break;
            case R.id.item_other:
                openActivity(EmvOtherActivity.class);
                break;
        }
    }


}
