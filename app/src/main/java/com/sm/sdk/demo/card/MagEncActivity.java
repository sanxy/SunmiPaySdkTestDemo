package com.sm.sdk.demo.card;

import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.Constant;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.card.wrapper.CheckCardCallbackV2Wrapper;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sm.sdk.demo.utils.Utility;
import com.sunmi.pay.hardware.aidl.AidlConstants;
import com.sunmi.pay.hardware.aidlv2.AidlConstantsV2;
import com.sunmi.pay.hardware.aidlv2.readcard.CheckCardCallbackV2;

import java.util.Locale;

public class MagEncActivity extends BaseAppCompatActivity {
    private static final String TAG = Constant.TAG;
    private static final int TDK_INDEX = 19;

    private MaterialButton mBtnTotal;
    private MaterialButton mBtnSuccess;
    private MaterialButton mBtnFail;
    private TextView mTvTrack1;
    private TextView mTvTrack2;
    private TextView mTvTrack3;
    private TextView mTvPAN;
    private TextView mTvCardholderName;
    private TextView mTvExpireDate;
    private TextView mTvServiceCode;
    private int mTotalTime;
    private int mSuccessTime;
    private int mFailTime;

    private Handler handler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_mag_en);
        initView();
        saveTDK();
        checkCard();
    }

    private void initView() {
        initToolbarBringBack(R.string.card_test_mag_enc);
        mBtnTotal = findViewById(R.id.mb_total);
        mBtnSuccess = findViewById(R.id.mb_success);
        mBtnFail = findViewById(R.id.mb_fail);
        mTvTrack1 = findViewById(R.id.tv_track1);
        mTvTrack2 = findViewById(R.id.tv_track2);
        mTvTrack3 = findViewById(R.id.tv_track3);
        mTvPAN = findViewById(R.id.tv_pan);
        mTvCardholderName = findViewById(R.id.tv_cardholder_name);
        mTvExpireDate = findViewById(R.id.tv_expire_date);
        mTvServiceCode = findViewById(R.id.tv_service_code);
    }

    /** Save a TDK(track data key) for test */
    private void saveTDK() {
        try {
            byte[] tdk = ByteUtil.hexStr2Bytes("F2914D44BC2AF05533DD20C9A0B5B861");
            byte[] tdkcv = ByteUtil.hexStr2Bytes("36821ADF5EB5513F");
            int code = MyApplication.mSecurityOptV2.savePlaintextKey(AidlConstants.Security.KEY_TYPE_TDK
                    , tdk, tdkcv, AidlConstants.Security.KEY_ALG_TYPE_3DES, TDK_INDEX);
            LogUtil.e(TAG, "save TDK " + (code == 0 ? "success" : "failed"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * start check card
     */
    private void checkCard() {
        Bundle bundle = new Bundle();
        bundle.putInt("cardType", AidlConstantsV2.CardType.MAGNETIC.getValue());
        bundle.putInt("encKeyIndex", TDK_INDEX);
        bundle.putInt("encMode", AidlConstantsV2.Security.DATA_MODE_ECB);
        bundle.putByteArray("encIv", new byte[16]);
        bundle.putByte("encPaddingMode", (byte) 0);
        bundle.putInt("encMaskStart", 6);
        bundle.putInt("encMaskEnd", 4);
        bundle.putChar("encMaskWord", '*');
        try {
            MyApplication.mReadCardOptV2.checkCardEnc(bundle, mCheckCardCallback, 60);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d(TAG, e.getMessage());
        }
    }

    private CheckCardCallbackV2 mCheckCardCallback = new CheckCardCallbackV2Wrapper() {
        /**
         * Find magnetic card
         *
         * @param bundle return data，contain the following keys:
         *             <br/>cardType: card type (int)
         *             <br/>TRACK1: track 1 data (String)
         *             <br/>TRACK2: track 2 data (String)
         *             <br/>TRACK3: track 3 data (String)
         *             <br/>pan: PAN data (String)
         *             <br/>name: cardholder name (String)
         *             <br/>expire: card expire date (String)
         *             <br/>servicecode: card service code (String)
         *             <br/>track1ErrorCode: track 1 error code (int)
         *             <br/>track2ErrorCode: track 2 error code (int)
         *             <br/>track3ErrorCode: track 3 error code (int)
         *             <br/> track error code is one of the following values:
         *             <ul>
         *             <li>0 - No error</li>
         *             <li>-1 - Track has no data</li>
         *             <li>-2 - Track parity check error</li>
         *             <li>-3 - Track LRC check error</li>
         *             </ul>
         */
        @Override
        public void findMagCard(Bundle bundle) throws RemoteException {
            LogUtil.e(Constant.TAG, "findMagCard");
            String pan = bundle.getString("pan");
            String name = bundle.getString("name");
            String expire = bundle.getString("expire");
            String serviceCode = bundle.getString("servicecode");
            LogUtil.e(Constant.TAG, "pan = " + pan + ",name = " + name + ",expire = " + expire + ",serviceCode = " + serviceCode);
            handleResult(bundle);
        }

        @Override
        public void findICCardEx(Bundle bundle) throws RemoteException {
            String atr = bundle.getString("atr");
            LogUtil.e(Constant.TAG, "findICCard,atr:" + atr);
        }

        @Override
        public void findRFCardEx(Bundle bundle) throws RemoteException {
            String uuid = bundle.getString("uuid");
            LogUtil.e(Constant.TAG, "findRFCard,uuid:" + uuid);
        }

        /**
         * Check card error
         *
         * @param bundle return data，contain the following keys:
         *             <br/>cardType: card type (int)
         *             <br/>code: the error code (String)
         *             <br/>message: the error message (String)
         */
        @Override
        public void onErrorEx(Bundle bundle) throws RemoteException {
            int code = bundle.getInt("code");
            String msg = bundle.getString("message");
            String error = "onError:" + msg + " -- " + code;
            LogUtil.e(Constant.TAG, error);
            showToast(error);
            handleResult(null);
        }
    };

    private void handleResult(Bundle bundle) {
        if (isFinishing()) {
            return;
        }
        handler.post(() -> {
            if (bundle == null) {
                showResult(false, "", "", "", bundle);
                return;
            }
            String track1 = Utility.null2String(bundle.getString("TRACK1"));
            String track2 = Utility.null2String(bundle.getString("TRACK2"));
            String track3 = Utility.null2String(bundle.getString("TRACK3"));
            //磁道错误码：0-无错误，-1-磁道无数据，-2-奇偶校验错，-3-LRC校验错
            int code1 = bundle.getInt("track1ErrorCode");
            int code2 = bundle.getInt("track2ErrorCode");
            int code3 = bundle.getInt("track3ErrorCode");
            LogUtil.e(TAG, String.format(Locale.getDefault(),
                    "track1ErrorCode:%d,track1:%s\ntrack2ErrorCode:%d,track2:%s\ntrack3ErrorCode:%d,track3:%s",
                    code1, track1, code2, track2, code3, track3));
            if ((code1 != 0 && code1 != -1) || (code2 != 0 && code2 != -1) || (code3 != 0 && code3 != -1)) {
                showResult(false, track1, track2, track3, bundle);
            } else {
                showResult(true, track1, track2, track3, bundle);
            }
            // 继续检卡
            if (!isFinishing()) {
                handler.postDelayed(this::checkCard, 500);
            }
        });
    }

    private void showResult(boolean success, String track1, String track2, String track3, Bundle bundle) {
        mTotalTime += 1;
        if (success) {
            mSuccessTime += 1;
        } else {
            mFailTime += 1;
        }
        mTvTrack1.setText(track1);
        mTvTrack2.setText(track2);
        mTvTrack3.setText(track3);
        if (bundle != null) {
            mTvPAN.setText(bundle.getString("pan"));
            mTvCardholderName.setText(bundle.getString("name"));
            mTvExpireDate.setText(bundle.getString("expire"));
            mTvServiceCode.setText(bundle.getString("servicecode"));
        }
        String temp = getString(R.string.card_total) + " " + mTotalTime;
        mBtnTotal.setText(temp);
        temp = getString(R.string.card_success) + " " + mSuccessTime;
        mBtnSuccess.setText(temp);
        temp = getString(R.string.card_fail) + " " + mFailTime;
        mBtnFail.setText(temp);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        cancelCheckCard();
        super.onDestroy();
    }

    private void cancelCheckCard() {
        try {
            MyApplication.mReadCardOptV2.cancelCheckCard();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
