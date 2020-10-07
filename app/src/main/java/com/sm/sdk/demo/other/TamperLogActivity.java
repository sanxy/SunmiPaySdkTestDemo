package com.sm.sdk.demo.other;

import android.os.Bundle;
import android.os.RemoteException;
import android.widget.Button;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.Constant;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.LogUtil;
import com.sunmi.pay.hardware.aidlv2.AidlConstantsV2;

/**
 * This is a test page, please do not refer to any code.
 * This page show how to get or clear tamper log.
 * At this moment,only P2Lite device support the tamper log function.
 */
public class TamperLogActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tamper_log);
        initView();
    }

    private void initView() {
        Button btn = findViewById(R.id.get_tamper_log);
        btn.setOnClickListener(v -> getTamperLog());
        btn = findViewById(R.id.clear_tamper_log);
        btn.setOnClickListener(v -> clearTamperLog());
    }

    /**
     * 获取触发Log
     */
    private void getTamperLog() {
        try {
            String log = MyApplication.mBasicOptV2.getSysParam(AidlConstantsV2.SysParam.TAMPER_LOG);
            LogUtil.e(Constant.TAG, "get tamper log:" + log);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 清除触发Log
     */
    private void clearTamperLog() {
        try {
            int code = MyApplication.mBasicOptV2.setSysParam(AidlConstantsV2.SysParam.TERM_STATUS, AidlConstantsV2.SysParam.CLEAR_TAMPER_LOG);
            LogUtil.e(Constant.TAG, "clear tamper log,code:" + code);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

}
