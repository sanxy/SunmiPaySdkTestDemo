package com.sm.sdk.demo.other;

import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.LogUtil;
import com.sm.sdk.demo.utils.Utility;
import com.sunmi.pay.hardware.aidl.AidlConstants;

public class PCDParamActivity extends BaseAppCompatActivity {
    private static final String TAG = "PCDParamActivity";
    private TextView tvParamA;
    private TextView tvParamB;
    private TextView tvParamC;
    private EditText edtParamA;
    private EditText edtParamB;
    private EditText edtParamC;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_pcd_param);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.setting_set_pcd_param);
        tvParamA = findViewById(R.id.tv_param_a);
        tvParamB = findViewById(R.id.tv_param_b);
        tvParamC = findViewById(R.id.tv_param_c);
        edtParamA = findViewById(R.id.edt_param_a);
        edtParamB = findViewById(R.id.edt_param_b);
        edtParamC = findViewById(R.id.edt_param_c);

        findViewById(R.id.mb_read).setOnClickListener(this);
        findViewById(R.id.mb_write_a).setOnClickListener(this);
        findViewById(R.id.mb_write_b).setOnClickListener(this);
        findViewById(R.id.mb_write_c).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mb_read:
                readPCDParam();
                break;
            case R.id.mb_write_a:
                setPCDParam(AidlConstants.SysParam.PCD_PARAM_A, edtParamA);
                break;
            case R.id.mb_write_b:
                setPCDParam(AidlConstants.SysParam.PCD_PARAM_B, edtParamB);
                break;
            case R.id.mb_write_c:
                setPCDParam(AidlConstants.SysParam.PCD_PARAM_C, edtParamC);
                break;
        }
    }

    private void readPCDParam() {
        try {
            String paramA = MyApplication.mBasicOptV2.getSysParam(AidlConstants.SysParam.PCD_PARAM_A);
            String paramB = MyApplication.mBasicOptV2.getSysParam(AidlConstants.SysParam.PCD_PARAM_B);
            String paramC = MyApplication.mBasicOptV2.getSysParam(AidlConstants.SysParam.PCD_PARAM_C);
            tvParamA.setText("PCD_PARAM_A:" + Utility.null2String(paramA));
            tvParamB.setText("PCD_PARAM_B:" + Utility.null2String(paramB));
            tvParamC.setText("PCD_PARAM_C:" + Utility.null2String(paramC));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void setPCDParam(String key, EditText edtValue) {
        try {
            String value = edtValue.getText().toString().trim();
            if (TextUtils.isEmpty(value)) {
                showToast(key + " shouldn't be empty, please reinput");
                edtValue.requestFocus();
                return;
            }
            int code = MyApplication.mBasicOptV2.setSysParam(key, value);
            if (code < 0) {
                String msg = "set " + key + " failed,code:" + code;
                showToast(msg);
                LogUtil.e(TAG, msg);
            } else {
                showToast("set " + key + " success");
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
