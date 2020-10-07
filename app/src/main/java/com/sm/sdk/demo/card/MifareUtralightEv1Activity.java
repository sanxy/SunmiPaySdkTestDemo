package com.sm.sdk.demo.card;

import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.Constant;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.card.wrapper.CheckCardCallbackV2Wrapper;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sunmi.pay.hardware.aidl.AidlConstants.CardType;
import com.sunmi.pay.hardware.aidlv2.AidlConstantsV2;
import com.sunmi.pay.hardware.aidlv2.readcard.CheckCardCallbackV2;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The Mifare Utralight ev1 card test page
 * (Test not passed, do not refer to any codes.)
 */
public class MifareUtralightEv1Activity extends BaseAppCompatActivity {

    private static final int OPT_INVALID = -1;     //非法操作
    private static final int OPT_AUTH_READ = 1;    //认证+读数据
    private static final int OPT_NO_AUTH_READ = 2; //不认证+读数据
    private static final int OPT_WRITE = 3;        //写数据
    private static final int OPT_INIT_AUTH = 4;    //初始化认证数据
    private static final int OPT_CLEAR_AUTH = 5;   //清除认证数据

    private int optType = OPT_INVALID;
    private UltralightEV1Helper helper;
    private CheckCardCallbackV2Impl callbackV2;
    private TextView result;
    private List<View> viewList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mifareutralightev1_layout);
        initData();
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.card_test_MIFARE_Ultralight_ev1);

        viewList.add(this.findViewById(R.id.auth_read));
        viewList.add(this.findViewById(R.id.no_auth_read));
        viewList.add(this.findViewById(R.id.write));
        viewList.add(this.findViewById(R.id.init_auth));
        viewList.add(this.findViewById(R.id.clear_auth));
        result = findViewById(R.id.result);
        for (View view : viewList) {
            view.setOnClickListener(this);
        }
    }

    private void initData() {
        viewList = new ArrayList<>();
        helper = new UltralightEV1Helper(this);
        callbackV2 = new CheckCardCallbackV2Impl();
    }

    private void toggleStatus(boolean status) {
        runOnUiThread(() -> {
            for (View view : viewList) {
                view.setEnabled(status);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.auth_read:
                handleButtonClick(OPT_AUTH_READ);
                break;
            case R.id.no_auth_read:
                handleButtonClick(OPT_NO_AUTH_READ);
                break;
            case R.id.write:
                handleButtonClick(OPT_WRITE);
                break;
            case R.id.init_auth:
                handleButtonClick(OPT_INIT_AUTH);
                break;
            case R.id.clear_auth:
                handleButtonClick(OPT_CLEAR_AUTH);
                break;
        }
    }

    private void handleButtonClick(int optType) {
        try {
            this.optType = optType;
            toggleStatus(false);
            MyApplication.mReadCardOptV2.checkCard(CardType.MIFARE.getValue(), callbackV2, 60);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private class CheckCardCallbackV2Impl extends CheckCardCallbackV2Wrapper {
        private byte[] authData;//认证数据

        @Override
        public void findMagCard(Bundle info) throws RemoteException {
            LogUtil.e(Constant.TAG, "findMagCard,info:" + info);
        }

        @Override
        public void findICCard(String atr) throws RemoteException {
            LogUtil.e(Constant.TAG, "findICCard,atr:" + atr);
        }

        @Override
        public void findRFCard(String uuid) throws RemoteException {
            LogUtil.e(Constant.TAG, "findRFCard,uuid:" + uuid);
            if (optType == OPT_AUTH_READ || optType == OPT_NO_AUTH_READ) {
                handleAuthenticate(uuid);
            } else if (optType == OPT_WRITE) {
                handleWriteData();
            } else if (optType == OPT_INIT_AUTH) {
                handleInitAuthData();
            } else if (optType == OPT_CLEAR_AUTH) {
                handleClearAuthData();
            }
        }

        @Override
        public void onError(int code, String message) throws RemoteException {
            showToast(code);
        }
    }

    /**
     * Auth/Non-Auth + Read data
     */
    private void handleAuthenticate(String uuid) {
        try {
            byte[] authData = helper.getAuthData(ByteUtil.hexStr2Bytes(uuid));
            if (authData == null) {
                return;
            }
            callbackV2.authData = authData;
            if (optType == OPT_NO_AUTH_READ) { //不认证+读数据
                int[] data = helper.readData();
                showData(uuid, data);
                return;
            }
            if (helper.authenticate(authData)) {//认证+读数据
                int[] data = helper.readData();
                showData(uuid, data);
            }
        } finally {
            toggleStatus(true);
        }
    }

    /**
     * Write data
     */
    private void handleWriteData() {
        try {
            boolean success = helper.write(0, 0x12345678);
            if (success) {
                success = helper.write(1, 0x87654321);
            }
            if (success) {
                success = helper.write(11, 0xa5a5a5a5);
            }
            LogUtil.e(Constant.TAG, "write data " + (success ? "success" : "failed"));
            showToast("write data " + (success ? "success" : "failed"));
        } finally {
            toggleStatus(false);
        }
    }

    /**
     * Initialize auth data
     */
    private void handleInitAuthData() {
        try {
            if (callbackV2.authData == null) {
                return;
            }
            boolean success = helper.initialize(callbackV2.authData);
            if (success) {
                success = helper.authenticate(callbackV2.authData);
            }
            if (success) {
                success = helper.initialize(callbackV2.authData);
            }
            LogUtil.e(Constant.TAG, "initialize auth data " + (success ? "success" : "failed"));
        } finally {
            toggleStatus(false);
        }
    }

    /**
     * clear auth data
     */
    private void handleClearAuthData() {
        try {
            boolean success = helper.writePW(16, 0xff000000);
            if (success) {
                success = helper.writePW(17, 0x0500);
            }
            if (success) {
                success = helper.writePW(18, 0xffffffff);
            }
            if (success) {
                success = helper.writePW(19, 0x00);
            }
            LogUtil.e(Constant.TAG, "clear auth data " + (success ? "success" : "failed"));
        } finally {
            toggleStatus(false);
        }
    }

    /**
     * Display data
     */
    private void showData(String uuid, int[] data) {
        StringBuilder sb = new StringBuilder(uuid);
        sb.append("\n");
        for (int i = 0; i < data.length; i++) {
            sb.append(String.format(Locale.getDefault(), "UL Data Idx %d : %08x%n", i, data[i]));
        }
        result.setText(sb);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelCheckCard();
    }

    private void cancelCheckCard() {
        try {
            MyApplication.mReadCardOptV2.cardOff(AidlConstantsV2.CardType.MIFARE.getValue());
            MyApplication.mReadCardOptV2.cancelCheckCard();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
