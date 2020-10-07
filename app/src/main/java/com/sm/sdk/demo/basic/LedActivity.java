package com.sm.sdk.demo.basic;

import android.os.Bundle;
import android.view.View;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.Constant;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.LogUtil;
import com.sunmi.pay.hardware.aidlv2.AidlConstantsV2;

public class LedActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_led);
        initToolbarBringBack(R.string.basic_led);
        initView();
    }

    private void initView() {
        findViewById(R.id.mb_blue_open).setOnClickListener(this);
        findViewById(R.id.mb_blue_close).setOnClickListener(this);

        findViewById(R.id.mb_yellow_open).setOnClickListener(this);
        findViewById(R.id.mb_yellow_close).setOnClickListener(this);

        findViewById(R.id.mb_green_open).setOnClickListener(this);
        findViewById(R.id.mb_green_close).setOnClickListener(this);

        findViewById(R.id.mb_red_open).setOnClickListener(this);
        findViewById(R.id.mb_red_close).setOnClickListener(this);

        findViewById(R.id.mb_all_open).setOnClickListener(this);
        findViewById(R.id.mb_all_close).setOnClickListener(this);


        findViewById(R.id.mb_new_rb_on).setOnClickListener(this);
        findViewById(R.id.mb_new_yg_on).setOnClickListener(this);
        findViewById(R.id.mb_new_all_on).setOnClickListener(this);
        findViewById(R.id.mb_new_all_off).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.mb_blue_open:
                ledStatus(AidlConstantsV2.LedLight.BLUE_LIGHT, 0);
                break;
            case R.id.mb_blue_close:
                ledStatus(AidlConstantsV2.LedLight.BLUE_LIGHT, 1);
                break;

            case R.id.mb_yellow_open:
                ledStatus(AidlConstantsV2.LedLight.YELLOW_LIGHT, 0);
                break;
            case R.id.mb_yellow_close:
                ledStatus(AidlConstantsV2.LedLight.YELLOW_LIGHT, 1);
                break;

            case R.id.mb_green_open:
                ledStatus(AidlConstantsV2.LedLight.GREEN_LIGHT, 0);
                break;
            case R.id.mb_green_close:
                ledStatus(AidlConstantsV2.LedLight.GREEN_LIGHT, 1);
                break;

            case R.id.mb_red_open:
                ledStatus(AidlConstantsV2.LedLight.RED_LIGHT, 0);
                break;
            case R.id.mb_red_close:
                ledStatus(AidlConstantsV2.LedLight.RED_LIGHT, 1);
                break;

            case R.id.mb_all_open:
                ledStatus(AidlConstantsV2.LedLight.BLUE_LIGHT, 0);
                ledStatus(AidlConstantsV2.LedLight.YELLOW_LIGHT, 0);
                ledStatus(AidlConstantsV2.LedLight.GREEN_LIGHT, 0);
                ledStatus(AidlConstantsV2.LedLight.RED_LIGHT, 0);
                break;
            case R.id.mb_all_close:
                ledStatus(AidlConstantsV2.LedLight.BLUE_LIGHT, 1);
                ledStatus(AidlConstantsV2.LedLight.YELLOW_LIGHT, 1);
                ledStatus(AidlConstantsV2.LedLight.GREEN_LIGHT, 1);
                ledStatus(AidlConstantsV2.LedLight.RED_LIGHT, 1);
                break;
            case R.id.mb_new_rb_on:
                ledStatusEx(0, 1, 1, 0);
                break;
            case R.id.mb_new_yg_on:
                ledStatusEx(1, 0, 0, 1);
                break;
            case R.id.mb_new_all_on:
                ledStatusEx(0, 0, 0, 0);
                break;
            case R.id.mb_new_all_off:
                ledStatusEx(1, 1, 1, 1);
                break;
        }
    }

    /**
     * operate LED light status
     *
     * @param ledIndex  The led index,1~4,1-Red，2-Green，3-Yellow，4-Blue
     * @param ledStatus LED Status，0-ON, 1-OFF
     */
    private void ledStatus(int ledIndex, int ledStatus) {
        try {
            int result = MyApplication.mBasicOptV2.ledStatusOnDevice(ledIndex, ledStatus);
            LogUtil.e(Constant.TAG, "result:" + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * operate LED light status
     *
     * @param redStatus    Red light, 0-ON, 1-OFF
     * @param greedStatus  Green light, 0-ON, 1-OFF
     * @param yellowStatus Yellow light, 0-ON, 1-OFF
     * @param blueStatus   Blue light, 0-ON, 1-OFF
     */
    private void ledStatusEx(int redStatus, int greedStatus, int yellowStatus, int blueStatus) {
        try {
            int result = MyApplication.mBasicOptV2.ledStatusOnDeviceEx(redStatus, greedStatus, yellowStatus, blueStatus);
            LogUtil.e(Constant.TAG, "result:" + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
