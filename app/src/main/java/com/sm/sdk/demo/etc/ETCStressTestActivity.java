package com.sm.sdk.demo.etc;

import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.Constant;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.LogUtil;
import com.sm.sdk.demo.utils.Utility;
import com.sunmi.pay.hardware.aidlv2.AidlErrorCodeV2;
import com.sunmi.pay.hardware.aidlv2.bean.ETCInfoV2;
import com.sunmi.pay.hardware.aidlv2.etc.ETCSearchListenerV2;

import java.util.List;

public class ETCStressTestActivity extends BaseAppCompatActivity {
    private static final String TAG = Constant.TAG;

    private EditText edtMaxDevNum;
    private EditText edtTimeout;
    private EditText edtIntervalTime;
    private Button btnTest;
    private Button btnTotal;
    private Button btnSuccess;
    private Button btnFailure;
    private TextView result;
    private Handler handler = new Handler();
    private boolean runningFlag;
    private int totalCount;
    private int successCount;
    private int failureCount;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_etc_stress_test_layout);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.etc_stress_test);
        edtMaxDevNum = findViewById(R.id.max_device_num);
        edtTimeout = findViewById(R.id.timeout_time);
        edtIntervalTime = findViewById(R.id.interval_time);
        btnTotal = findViewById(R.id.mb_total);
        btnSuccess = findViewById(R.id.mb_success);
        btnFailure = findViewById(R.id.mb_fail);
        result = findViewById(R.id.result);
        btnTest = findViewById(R.id.mb_stress_test);
        btnTest.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mb_stress_test:
                handleStressTest(v);
                break;
            default:
                break;
        }
    }

    private void handleStressTest(View view) {
        if (runningFlag) {//测试中
            stopStressTest();
            runningFlag = false;
            btnTest.setText(R.string.etc_start_stress_test);
        } else {//测试未开始
            updateCounter(0, false);
            startStressTest();
            runningFlag = true;
            btnTest.setText(R.string.etc_stop_stress_test);
        }
    }

    private void startStressTest() {
        updateResultText(null);
        if (checkSearchInput()) {
            searchETCDevice();
        }
    }

    private void stopStressTest() {
        handler.removeCallbacksAndMessages(null);
    }

    /** 更新显示值 */
    private void updateResultText(CharSequence value) {
        handler.post(() -> result.setText(value));
    }

    private boolean checkSearchInput() {
        String maxDevNumStr = edtMaxDevNum.getText().toString();
        if (TextUtils.isEmpty(maxDevNumStr)) {
            edtMaxDevNum.requestFocus();
            showToast("Max search ETC device number shouldn't be empty");
            return false;
        }
        int maxSearchNum = Integer.parseInt(maxDevNumStr);
        if (maxSearchNum < 0) {
            edtMaxDevNum.requestFocus();
            showToast("Max search ETC device number should >=0");
            return false;
        }
        String timeoutStr = edtTimeout.getText().toString();
        if (TextUtils.isEmpty(timeoutStr)) {
            edtTimeout.requestFocus();
            showToast("Timeout time shouldn't be empty");
            return false;
        }
        int time = Integer.parseInt(timeoutStr);
        if (time < 0) {
            edtTimeout.requestFocus();
            showToast("Timeout time should >=0");
            return false;
        }

        String intervalStr = edtIntervalTime.getText().toString();
        if (TextUtils.isEmpty(intervalStr)) {
            edtIntervalTime.requestFocus();
            showToast("Interval time shouldn't be empty");
            return false;
        }
        int intervalTime = Integer.parseInt(intervalStr);
        if (intervalTime < 0) {
            edtIntervalTime.requestFocus();
            showToast("Interval time should >=0");
            return false;
        }
        return true;
    }

    private void searchETCDevice() {
        try {
            int maxSearchNum = Integer.parseInt(edtMaxDevNum.getText().toString());
            int timeout = Integer.parseInt(edtTimeout.getText().toString());
            int interval = Integer.parseInt(edtIntervalTime.getText().toString());
            MyApplication.mETCOptV2.search(maxSearchNum, new ETCSearchListenerV2.Stub() {
                @Override
                public void onSuccess(List<ETCInfoV2> list) throws RemoteException {
                    updateCounter(1, true);
                    showETCDevices(list);
                    handler.postDelayed(() -> startStressTest(), interval);
                }

                @Override
                public void onError(int code) throws RemoteException {
                    String msg = AidlErrorCodeV2.valueOf(code).getMsg();
                    LogUtil.e(TAG, "Search ETC device error,code:" + code + ",msg:" + msg);
                    updateCounter(1, false);
                    handler.postDelayed(() -> startStressTest(), interval);
                }
            }, timeout);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateCounter(int optType, boolean success) {
        handler.post(() -> {
            if (optType == 0) {//清除
                totalCount = 0;
                successCount = 0;
                failureCount = 0;
            } else {
                totalCount++;
                if (success) {
                    successCount++;
                } else {
                    failureCount++;
                }
            }
            String totalStr = Utility.formatStr("%s %d", getString(R.string.card_total), totalCount);
            String successStr = Utility.formatStr("%s %d", getString(R.string.card_success), successCount);
            String failure = Utility.formatStr("%s %d", getString(R.string.card_fail), failureCount);
            btnTotal.setText(totalStr);
            btnSuccess.setText(successStr);
            btnFailure.setText(failure);
        });
    }

    private void showETCDevices(List<ETCInfoV2> list) {
        StringBuilder sb = new StringBuilder();
        for (ETCInfoV2 dev : list) {
            //设备编号
            sb.append(getString(R.string.etc_i2c_device_No));
            sb.append(Utility.null2String(dev.deviceNo));
            sb.append("\n");
            //设备状态
            sb.append(getString(R.string.etc_i2c_device_status));
            sb.append(Utility.null2String(dev.deviceStatus));
            sb.append("\n");
            //设备金额
            sb.append(getString(R.string.etc_i2c_amount));
            if ("00".equals(dev.cardType)) {//储值卡
                sb.append(getString(R.string.etc_i2c_card_type_1));
                //储值卡显示金额
                sb.append(" ");
                sb.append(dev.amount);
                sb.append(getString(R.string.etc_i2c_amount_unit));
            } else if ("01".equals(dev.cardType)) {//记账卡
                sb.append(getString(R.string.etc_i2c_card_type_2));
            } else if ("02".equals(dev.cardType)) {//非法卡片
                sb.append(getString(R.string.etc_i2c_card_type_3));
            }
            sb.append("\n");
            //车牌颜色
            sb.append(getString(R.string.etc_i2c_plate_color));
            sb.append(Utility.null2String(dev.licensePlateColor));
            sb.append("\n");
            //车牌号码
            sb.append(getString(R.string.etc_i2c_plate_No));
            sb.append(Utility.null2String(dev.licensePlateNo));
            sb.append("\n");
            //信号强度
            sb.append(getString(R.string.etc_i2c_signal_level));
            sb.append(dev.signal);
            sb.append("\n\n");
        }
        updateResultText(sb);
    }

    @Override
    protected void onDestroy() {
        stopStressTest();
        super.onDestroy();
    }

}
