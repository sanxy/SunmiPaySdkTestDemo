package com.sm.sdk.demo.emv;

import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.Constant;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.card.wrapper.CheckCardCallbackV2Wrapper;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sm.sdk.demo.utils.ThreadPoolUtil;
import com.sm.sdk.demo.utils.Utility;
import com.sunmi.pay.hardware.aidlv2.AidlConstantsV2;
import com.sunmi.pay.hardware.aidlv2.AidlErrorCodeV2;
import com.sunmi.pay.hardware.aidlv2.bean.PinPadConfigV2;
import com.sunmi.pay.hardware.aidlv2.pinpad.PinPadListenerV2;
import com.sunmi.pay.hardware.aidlv2.pinpad.PinPadOptV2;
import com.sunmi.pay.hardware.aidlv2.readcard.CheckCardCallbackV2;
import com.sunmi.pay.hardware.aidlv2.readcard.ReadCardOptV2;

public class MagProcessActivity extends BaseAppCompatActivity {
    private static final String TAG = "MagProcessActivity";
    private EditText mEditAmount;
    private TextView mTvShowInfo;
    private PinPadOptV2 mPinPadOptV2;
    private ReadCardOptV2 mReadCardOptV2;
    private String mCardNo;

    private static final int PIN_INIT = 1;
    private static final int PIN_CLICK_NUMBER = 2;
    private static final int PIN_CLICK_PIN = 3;
    private static final int PIN_CLICK_CONFIRM = 4;
    private static final int PIN_CLICK_CANCEL = 5;
    private static final int PIN_ERROR = 6;

    private Handler mHandler = new Handler(msg -> {
        switch (msg.what) {
            case PIN_INIT:
                dismissLoadingDialog();
                initPinPad();
                break;
            case PIN_CLICK_NUMBER:
                break;
            case PIN_CLICK_PIN:
                mockRequestToServer();
                break;
            case PIN_CLICK_CONFIRM:
                mockRequestToServer();
                break;
            case PIN_CLICK_CANCEL:
                showToast("user cancel");
                break;
            case PIN_ERROR:
                showToast("error:" + msg.obj + " -- " + msg.arg1);
                break;
        }
        return false;
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emv_ic);
        initView();
        ThreadPoolUtil.executeInCachePool(EmvUtil::initKey);
    }

    private void initView() {
        mPinPadOptV2 = MyApplication.mPinPadOptV2;
        mReadCardOptV2 = MyApplication.mReadCardOptV2;
        initToolbarBringBack(R.string.emv_mag_process);
        mEditAmount = findViewById(R.id.edit_amount);
        mTvShowInfo = findViewById(R.id.tv_info);
        findViewById(R.id.mb_ok).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.mb_ok:
                mCardNo = null;
                mTvShowInfo.setText("");
                String amount = mEditAmount.getText().toString();
                try {
                    long parseLong = Long.parseLong(amount);
                    if (parseLong > 0) {
                        checkCard();
                    } else {
                        showToast(R.string.card_cost_hint);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showToast(R.string.card_cost_hint);
                }
                break;
        }
    }

    private void checkCard() {
        try {
            showLoadingDialog(R.string.emv_swing_card_mag);
            int cardType = AidlConstantsV2.CardType.MAGNETIC.getValue();
            mReadCardOptV2.checkCard(cardType, mCheckCardCallback, 60);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initPinPad() {
        LogUtil.e(Constant.TAG, "initPinPad");
        try {
            PinPadConfigV2 pinPadConfig = new PinPadConfigV2();
            pinPadConfig.setPinPadType(0);
            pinPadConfig.setPinType(0);
            pinPadConfig.setOrderNumKey(false);
            byte[] panBytes = mCardNo.substring(mCardNo.length() - 13, mCardNo.length() - 1).getBytes("US-ASCII");
            pinPadConfig.setPan(panBytes);
            pinPadConfig.setTimeout(60 * 1000); // input password timeout
            pinPadConfig.setPinKeyIndex(12);    // pik index
            pinPadConfig.setMaxInput(12);
            pinPadConfig.setMinInput(0);
            mPinPadOptV2.initPinPad(pinPadConfig, mPinPadListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mockRequestToServer() {
        ThreadPoolUtil.executeInCachePool(() -> {
            try {
                showLoadingDialog(R.string.requesting);
                Thread.sleep(1500);
                showToast(R.string.success);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                dismissLoadingDialog();
            }
        });
    }

    private CheckCardCallbackV2 mCheckCardCallback = new CheckCardCallbackV2Wrapper() {
        @Override
        public void findMagCard(Bundle bundle) throws RemoteException {
            LogUtil.e(Constant.TAG, "findMagCard");
            String track1 = Utility.null2String(bundle.getString("TRACK1"));
            String track2 = Utility.null2String(bundle.getString("TRACK2"));
            String track3 = Utility.null2String(bundle.getString("TRACK3"));
            runOnUiThread(() -> {
                String value = "track1:" + track1 + "\ntrack2:" + track2 + "\ntrack3:" + track3;
                mTvShowInfo.setText(value);
            });
            if (!TextUtils.isEmpty(track2)) {
                int index = track2.indexOf("=");
                if (index != -1) {
                    mCardNo = track2.substring(0, index);
                }
            }
            if (!TextUtils.isEmpty(mCardNo)) {
                mHandler.obtainMessage(PIN_INIT).sendToTarget();
            } else {
                dismissLoadingDialog();
                showToast(R.string.emv_card_no_error);
            }
        }

        @Override
        public void findICCard(String atr) throws RemoteException {
            LogUtil.e(Constant.TAG, "findICCard,atr:" + atr);
        }

        @Override
        public void findRFCard(String uuid) throws RemoteException {
            LogUtil.e(Constant.TAG, "findRFCard,atr:" + uuid);
        }

        @Override
        public void onError(int code, String message) throws RemoteException {
            String error = "onError:" + message + " -- " + code;
            LogUtil.e(Constant.TAG, error);
            showToast(error);
            dismissLoadingDialog();
        }
    };

    private PinPadListenerV2 mPinPadListener = new PinPadListenerV2.Stub() {

        @Override
        public void onPinLength(int len) {
            LogUtil.e(Constant.TAG, "onPinLength:" + len);
            mHandler.obtainMessage(PIN_CLICK_NUMBER, len).sendToTarget();
        }

        @Override
        public void onConfirm(int i, byte[] pinBlock) {
            if (pinBlock != null) {
                String hexStr = ByteUtil.bytes2HexStr(pinBlock);
                LogUtil.e(Constant.TAG, "onConfirm pin block:" + hexStr);
                mHandler.obtainMessage(PIN_CLICK_PIN, pinBlock).sendToTarget();
            } else {
                mHandler.obtainMessage(PIN_CLICK_CONFIRM).sendToTarget();
            }
        }

        @Override
        public void onCancel() {
            LogUtil.e(Constant.TAG, "onCancel");
            mHandler.obtainMessage(PIN_CLICK_CANCEL).sendToTarget();
        }

        @Override
        public void onError(int code) {
            LogUtil.e(Constant.TAG, "onError:" + code);
            String msg = AidlErrorCodeV2.valueOf(code).getMsg();
            mHandler.obtainMessage(PIN_ERROR, code, code, msg).sendToTarget();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelCheckCard();
    }

    private void cancelCheckCard() {
        try {
            mReadCardOptV2.cancelCheckCard();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
