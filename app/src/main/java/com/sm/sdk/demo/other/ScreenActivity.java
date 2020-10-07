package com.sm.sdk.demo.other;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.LogUtil;

public class ScreenActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int screen_orientation = getIntent().getIntExtra("SCREEN_ORIENTATION", ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        LogUtil.e("lxy","screen_orientation:"+screen_orientation);
        setRequestedOrientation(screen_orientation);
        setContentView(R.layout.activity_screen);
        initToolbarBringBack(R.string.screen_rotation);
    }

    public void right(View view) {
        setAction(this, ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
    }

    public void left(View view) {
        setAction(this, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    public void down(View view) {
        setAction(this, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    public void up(View view) {
        setAction(this, ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
    }

    private static void setAction(ScreenActivity context, int SCREEN_ORIENTATION) {
        Intent intent = new Intent(context, ScreenActivity.class);
        intent.putExtra("SCREEN_ORIENTATION", SCREEN_ORIENTATION);
        context.startActivity(intent);
        context.finish();
    }

}
