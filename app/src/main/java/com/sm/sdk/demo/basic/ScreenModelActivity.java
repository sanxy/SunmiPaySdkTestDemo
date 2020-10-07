package com.sm.sdk.demo.basic;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.widget.Toolbar;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sunmi.pay.hardware.aidlv2.AidlConstantsV2;
import com.sunmi.pay.hardware.aidlv2.system.BasicOptV2;

public class ScreenModelActivity extends BaseAppCompatActivity {
    private BasicOptV2 basicOptV2 = MyApplication.mBasicOptV2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_screen_model);
        initView();
    }

    private void initView() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.basic_screen_mode);
        toolbar.setNavigationIcon(R.drawable.ic_back_white);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(
                v -> onBackPressed()
        );
        findViewById(R.id.mb_set_screen_monopoly).setOnClickListener(this);
        findViewById(R.id.mb_clear_screen_monopoly).setOnClickListener(this);
        findViewById(R.id.mb_disable_status_bar_drop_down).setOnClickListener(this);
        findViewById(R.id.mb_enable_status_bar_drop_down).setOnClickListener(this);
        findViewById(R.id.mb_hide_nav_bar).setOnClickListener(this);
        findViewById(R.id.mb_show_nav_bar).setOnClickListener(this);
        findViewById(R.id.mb_hide_nav_back).setOnClickListener(this);
        findViewById(R.id.mb_hide_nav_home).setOnClickListener(this);
        findViewById(R.id.mb_hide_nav_recent).setOnClickListener(this);
        findViewById(R.id.mb_hide_nav_back_and_home).setOnClickListener(this);
        findViewById(R.id.mb_hide_nav_back_and_recent).setOnClickListener(this);
        findViewById(R.id.mb_hide_nav_home_and_recent).setOnClickListener(this);
        findViewById(R.id.mb_hide_nav_all).setOnClickListener(this);
        findViewById(R.id.mb_show_nav_all).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        try {
            switch (v.getId()) {
                case R.id.mb_set_screen_monopoly:
                    basicOptV2.setScreenMode(AidlConstantsV2.SystemUI.SET_SCREEN_MONOPOLY);
                    break;
                case R.id.mb_clear_screen_monopoly:
                    basicOptV2.setScreenMode(AidlConstantsV2.SystemUI.CLEAR_SCREEN_MONOPOLY);
                    break;
                case R.id.mb_disable_status_bar_drop_down:
                    basicOptV2.setStatusBarDropDownMode(AidlConstantsV2.SystemUI.DISABLE_STATUS_BAR_DROP_DOWN);
                    break;
                case R.id.mb_enable_status_bar_drop_down:
                    basicOptV2.setStatusBarDropDownMode(AidlConstantsV2.SystemUI.ENABLE_STATUS_BAR_DROP_DOWN);
                    break;
                case R.id.mb_hide_nav_bar:
                    basicOptV2.setNavigationBarVisibility(AidlConstantsV2.SystemUI.HIDE_NAV_BAR);
                    break;
                case R.id.mb_show_nav_bar:
                    basicOptV2.setNavigationBarVisibility(AidlConstantsV2.SystemUI.SHOW_NAV_BAR);
                    break;
                case R.id.mb_hide_nav_back:
                    basicOptV2.setHideNavigationBarItems(AidlConstantsV2.SystemUI.HIDE_NAV_ITEM_BACK_KEY);
                    break;
                case R.id.mb_hide_nav_home:
                    basicOptV2.setHideNavigationBarItems(AidlConstantsV2.SystemUI.HIDE_NAV_ITEM_HOME_KEY);
                    break;
                case R.id.mb_hide_nav_recent:
                    basicOptV2.setHideNavigationBarItems(AidlConstantsV2.SystemUI.HIDE_NAV_ITEM_RECENT_KEY);
                    break;
                case R.id.mb_hide_nav_back_and_home:
                    basicOptV2.setHideNavigationBarItems(AidlConstantsV2.SystemUI.HIDE_NAV_ITEM_BACK_KEY
                            | AidlConstantsV2.SystemUI.HIDE_NAV_ITEM_HOME_KEY);
                    break;
                case R.id.mb_hide_nav_back_and_recent:
                    basicOptV2.setHideNavigationBarItems(AidlConstantsV2.SystemUI.HIDE_NAV_ITEM_BACK_KEY
                            | AidlConstantsV2.SystemUI.HIDE_NAV_ITEM_RECENT_KEY);
                    break;
                case R.id.mb_hide_nav_home_and_recent:
                    basicOptV2.setHideNavigationBarItems(AidlConstantsV2.SystemUI.HIDE_NAV_ITEM_HOME_KEY
                            | AidlConstantsV2.SystemUI.HIDE_NAV_ITEM_RECENT_KEY);
                    break;
                case R.id.mb_hide_nav_all:
                    basicOptV2.setHideNavigationBarItems(AidlConstantsV2.SystemUI.HIDE_NAV_ITEM_BACK_KEY
                            | AidlConstantsV2.SystemUI.HIDE_NAV_ITEM_HOME_KEY
                            | AidlConstantsV2.SystemUI.HIDE_NAV_ITEM_RECENT_KEY);
                    break;
                case R.id.mb_show_nav_all:
                    basicOptV2.setHideNavigationBarItems(0);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            basicOptV2.setScreenMode(AidlConstantsV2.SystemUI.CLEAR_SCREEN_MONOPOLY);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
