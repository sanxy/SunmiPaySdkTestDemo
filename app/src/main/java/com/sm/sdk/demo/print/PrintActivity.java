package com.sm.sdk.demo.print;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.R;

public class PrintActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print);
        initToolbarBringBack(R.string.print);
        initView();
    }

    private void initView(){
        View item = this.findViewById(R.id.print_text);
        TextView leftText = item.findViewById(R.id.left_text);
        leftText.setText(R.string.print_text);
        item.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.print_text:
                openActivity(PrintTextActivity.class);
                break;
        }
    }
}
