package com.sm.sdk.demo.etc;

import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
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

public class SearchETCDeviceActivity extends BaseAppCompatActivity {
    private static final String TAG = Constant.TAG;

    private EditText edtChannel;
    private EditText edtTransPower;
    private EditText edtBuzzer;
    private EditText edtFragTimeout;
    private EditText edtMaxDevNum;
    private EditText edtTimeout;
    private TextView result;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_etc_search_layout);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.etc_search_etc_device);
        edtChannel = findViewById(R.id.comm_channel);
        edtTransPower = findViewById(R.id.search_trans_power);
        edtBuzzer = findViewById(R.id.search_buzzer);
        edtFragTimeout = findViewById(R.id.search_frag_timeout);
        edtMaxDevNum = findViewById(R.id.max_device_num);
        edtTimeout = findViewById(R.id.timeout_time);
        findViewById(R.id.mb_set_param).setOnClickListener(this);
        findViewById(R.id.mb_search).setOnClickListener(this);
        result = findViewById(R.id.result);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mb_set_param:
                updateResultText(null);
                if (checkSetParamInput()) {
                    setSearchParam();
                }
                break;
            case R.id.mb_search:
                updateResultText(null);
                if (checkSearchInput()) {
                    searchETCDevice();
                }
                break;
        }
    }

    /** 更新显示值 */
    private void updateResultText(CharSequence value) {
        runOnUiThread(() -> result.setText(value));
    }

    private boolean checkSetParamInput() {
        String channelStr = edtChannel.getText().toString();
        if (TextUtils.isEmpty(channelStr)) {
            edtChannel.requestFocus();
            showToast("Communication channel shouldn't be empty");
            return false;
        }
        int channel = Integer.parseInt(channelStr);
        if (channel < 0 || channel > 1) {
            edtChannel.requestFocus();
            showToast("Communication channel should in [0, 1]");
            return false;
        }
        String transPowerStr = edtTransPower.getText().toString();
        if (TextUtils.isEmpty(transPowerStr)) {
            edtTransPower.requestFocus();
            showToast("Transmit power shouldn't be empty");
            return false;
        }
        int transPower = Integer.parseInt(transPowerStr);
        if (transPower < 0 || transPower > 3) {
            edtTransPower.requestFocus();
            showToast("Transmit power should in [0, 3]");
            return false;
        }

        String buzzerStr = edtBuzzer.getText().toString();
        if (TextUtils.isEmpty(buzzerStr)) {
            edtBuzzer.requestFocus();
            showToast("Buzzer shouldn't be empty");
            return false;
        }
        int buzzer = Integer.parseInt(buzzerStr);
        if (buzzer < 0 || buzzer > 1) {
            edtBuzzer.requestFocus();
            showToast("Buzzer should in [0, 1]");
            return false;
        }
        String fragTimeoutStr = edtFragTimeout.getText().toString();
        if (TextUtils.isEmpty(fragTimeoutStr)) {
            edtFragTimeout.requestFocus();
            showToast("Fragment timeout time shouldn't be empty");
            return false;
        }
        int fragTimeout = Integer.parseInt(fragTimeoutStr);
        if (fragTimeout < 0 || fragTimeout > 60) {
            edtFragTimeout.requestFocus();
            showToast("Fragment timeout time should in [0, 60]");
            return false;
        }
        return true;
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
        return true;
    }

    /** Set search param */
    private void setSearchParam() {
        try {
            int channel = Integer.parseInt(edtChannel.getText().toString());
            int transPower = Integer.parseInt(edtTransPower.getText().toString());
            int fragTimeout = Integer.parseInt(edtFragTimeout.getText().toString());
            int buzzer = Integer.parseInt(edtBuzzer.getText().toString());
            Bundle bundle = new Bundle();
            bundle.putInt("channel", channel);
            bundle.putInt("transPower", transPower);
            bundle.putInt("buzzer", buzzer);
            bundle.putInt("fragTimeout", fragTimeout);
            int code = MyApplication.mETCOptV2.setSearchParam(bundle);
            if (code < 0) {
                String msg = "ETC setSearchParam() error,code:" + code;
                LogUtil.e(TAG, msg);
                showToast(msg);
            } else {
                String msg = "ETC setSearchParam() success";
                Log.e(TAG, msg);
                showToast(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void searchETCDevice() {
        try {
            int maxSearchNum = Integer.parseInt(edtMaxDevNum.getText().toString());
            int timeout = Integer.parseInt(edtTimeout.getText().toString());
            MyApplication.mETCOptV2.search(maxSearchNum, new ETCSearchListenerV2.Stub() {
                @Override
                public void onSuccess(List<ETCInfoV2> list) throws RemoteException {
                    showETCDevices(list);
                }

                @Override
                public void onError(int code) throws RemoteException {
                    String msg = AidlErrorCodeV2.valueOf(code).getMsg();
                    LogUtil.e(TAG, "Search ETC device error,code:" + code + ",msg:" + msg);
                }
            }, timeout);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        super.onDestroy();
        cancelSearch();
    }

    private void cancelSearch() {
        try {
            MyApplication.mETCOptV2.cancelSearch();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
