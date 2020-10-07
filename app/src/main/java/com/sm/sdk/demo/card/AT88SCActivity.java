package com.sm.sdk.demo.card;

import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.Constant;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.card.wrapper.CheckCardCallbackV2Wrapper;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sunmi.pay.hardware.aidlv2.AidlConstantsV2;
import com.sunmi.pay.hardware.aidlv2.readcard.CheckCardCallbackV2;

import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;

public class AT88SCActivity extends BaseAppCompatActivity {
    private static final String TAG = Constant.TAG;

    private EditText edtAuthKey;
    private EditText edtAuthRWFlag;
    private EditText edtAuthZoneNo;
    private EditText edtChgOldKey;
    private EditText edtChgNewKey;
    private EditText edtChgKeyRWFlag;
    private EditText edtChgKeyZoneNo;
    private EditText edtReadStartAddr;
    private EditText edtReadLen;
    private EditText edtReadZoneFlag;
    private EditText edtReadResult;
    private EditText edtWriteStartAddr;
    private EditText edtWriteZoneFlag;
    private EditText edtWriteData;

    private EditText edtRemainAuthRWFlag;
    private EditText edtRemainAuthZoneNo;
    private TextView txtRemainAuthCount;

    private int cardType = AidlConstantsV2.CardType.AT88SC1608.getValue();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_at88sc_1608);
        initView();
        checkCard();
    }

    private void initView() {
        initToolbarBringBack(R.string.card_test_at88scxx);
        edtAuthKey = findViewById(R.id.edt_auth_key);
        edtAuthRWFlag = findViewById(R.id.edt_auth_rw_flag);
        edtAuthZoneNo = findViewById(R.id.edt_auth_zone_no);

        edtChgOldKey = findViewById(R.id.edt_chg_key_old_key);
        edtChgNewKey = findViewById(R.id.edt_chg_key_new_key);
        edtChgKeyRWFlag = findViewById(R.id.edt_chg_key_rw_flag);
        edtChgKeyZoneNo = findViewById(R.id.edt_chg_key_zone_no);

        edtReadStartAddr = findViewById(R.id.edt_read_data_start_address);
        edtReadLen = findViewById(R.id.edt_read_data_len);
        edtReadZoneFlag = findViewById(R.id.edt_read_data_zone_flag);
        edtReadResult = findViewById(R.id.edt_read_data_result);

        edtWriteStartAddr = findViewById(R.id.edt_write_start_address);
        edtWriteZoneFlag = findViewById(R.id.edt_write_data_zone_flag);
        edtWriteData = findViewById(R.id.edt_write_data);

        edtRemainAuthRWFlag = findViewById(R.id.edt_read_remain_rw_flag);
        edtRemainAuthZoneNo = findViewById(R.id.edt_read_remain_zone_no);
        txtRemainAuthCount = findViewById(R.id.txt_remain_count);

        findViewById(R.id.mb_auth).setOnClickListener(this);
        findViewById(R.id.mb_chg_key).setOnClickListener(this);
        findViewById(R.id.mb_read_data).setOnClickListener(this);
        findViewById(R.id.mb_write_data).setOnClickListener(this);
        findViewById(R.id.mb_read_remain_count).setOnClickListener(this);
        RadioGroup group = findViewById(R.id.rdo_group_card_type);
        group.setOnCheckedChangeListener((group1, checkedId) -> {
            if (checkedId == R.id.rdo_at88sc1608) {
                cardType = AidlConstantsV2.CardType.AT88SC1608.getValue();
                checkCard();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mb_auth:
                at88scAuthKey(edtAuthKey, edtAuthRWFlag, edtAuthZoneNo);
                break;
            case R.id.mb_chg_key:
                at88scChangeKey();
                break;
            case R.id.mb_read_data:
                at88scReadData();
                break;
            case R.id.mb_write_data:
                at88scWriteData();
                break;
            case R.id.mb_read_remain_count:
                at88scGetRemainAuthCount();
                break;
        }
    }

    private void checkCard() {
        try {
            showSwingCardHintDialog();
            // card type can be AT88SC1608
            MyApplication.mReadCardOptV2.checkCard(cardType, mCheckCardCallback, 60);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private CheckCardCallbackV2 mCheckCardCallback = new CheckCardCallbackV2Wrapper() {

        @Override
        public void findMagCard(Bundle bundle) throws RemoteException {
            LogUtil.e(TAG, "findMagCard");
            dismissSwingCardHintDialog();
        }

        @Override
        public void findICCard(String atr) throws RemoteException {
            LogUtil.e(TAG, "findICCard:" + atr);
            dismissSwingCardHintDialog();
        }

        @Override
        public void findRFCard(String uuid) throws RemoteException {
            LogUtil.e(TAG, "findRFCard:" + uuid);
            dismissSwingCardHintDialog();
        }

        @Override
        public void onError(int code, String message) throws RemoteException {
            LogUtil.e(TAG, "检卡失败，code:" + code + ",msg:" + message);
            checkCard();
        }
    };

    /** AT88SCxxx verify password */
    private boolean at88scAuthKey(EditText edtKey, EditText edtRWFlg, EditText edtZoneNo) {
        try {
            if (!checkInputKey(edtKey) || !checkInputRWFlag(edtRWFlg) || !checkInputZoneNo(edtZoneNo)) {
                return false;
            }
            String key = edtKey.getText().toString();
            byte[] keyBytes = ByteUtil.hexStr2Bytes(key);
            int rwFlag = Integer.valueOf(edtRWFlg.getText().toString());
            int zoneNo = Integer.valueOf(edtZoneNo.getText().toString());
            int code = MyApplication.mReadCardOptV2.at88scAuthKey(keyBytes, rwFlag, zoneNo);
            if (code != 0) {
                showToast(" at88scAuthKey failed");
                LogUtil.e(TAG, "at88scAuthKey failed,code:" + code);
                return false;
            }
            showToast(" at88scAuthKey success");
            LogUtil.e(TAG, "at88scAuthKey success");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /** AT88SCxxx change password */
    private boolean at88scChangeKey() {
        try {
            if (!checkInputKey(edtChgOldKey) || !checkInputKey(edtChgNewKey)
                    || !checkInputRWFlag(edtChgKeyRWFlag) || !checkInputZoneNo(edtChgKeyZoneNo)) {
                return false;
            }
            if (!at88scAuthKey(edtChgOldKey, edtChgKeyRWFlag, edtChgKeyZoneNo)) {
                LogUtil.e(TAG, "at88scChangeKey failed");
                showToast("at88scChangeKey failed");
                return false;
            }
            String newKey = edtChgNewKey.getText().toString();
            byte[] newKeyBytes = ByteUtil.hexStr2Bytes(newKey);
            int rwFlag = Integer.valueOf(edtChgKeyRWFlag.getText().toString());
            int zoneNo = Integer.valueOf(edtChgKeyZoneNo.getText().toString());
            int code = MyApplication.mReadCardOptV2.at88scChangeKey(newKeyBytes, rwFlag, zoneNo);
            if (code != 0) {
                showToast(" at88scChangeKey failed");
                LogUtil.e(TAG, "at88scChangeKey failed,code:" + code);
                return false;
            }
            showToast(" at88scChangeKey success");
            LogUtil.e(TAG, "at88scChangeKey success");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /** AT88SCxxx read data */
    private boolean at88scReadData() {
        try {
            if (!checkInputStartAddress(edtReadStartAddr) || !checkInputLength(edtReadLen)
                    || !checkInputZoneFlag(edtReadZoneFlag)) {
                return false;
            }
            String startAddress = edtReadStartAddr.getText().toString();
            String length = edtReadLen.getText().toString();
            int startAddr = Integer.parseInt(startAddress, 16);
            int len = Integer.parseInt(length);
            //zoneFlag,0-Configuration zone, 1-User zone
            int zoneFlag = Integer.valueOf(edtReadZoneFlag.getText().toString());
            byte[] out = new byte[260];
            int retLen = MyApplication.mReadCardOptV2.at88scReadData(startAddr, len, zoneFlag, out);
            if (retLen < 0) {
                showToast(" at88scReadData failed");
                LogUtil.e(TAG, "at88scReadData failed,code:" + retLen);
                return false;
            }
            String data = ByteUtil.bytes2HexStr(Arrays.copyOf(out, retLen));
            edtReadResult.setText(data);
            LogUtil.e(TAG, "at88scReadData success, data:" + data);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /** AT88SCxxx write data */
    private boolean at88scWriteData() {
        try {
            if (!checkInputStartAddress(edtWriteStartAddr) || !checkInputZoneFlag(edtWriteZoneFlag)
                    || !checkInputData(edtWriteData)) {
                return false;
            }
            String startAddress = edtWriteStartAddr.getText().toString();
            int startAddr = Integer.valueOf(startAddress, 16);
            int zoneFlag = Integer.valueOf(edtWriteZoneFlag.getText().toString());
            byte[] data = ByteUtil.hexStr2Bytes(edtWriteData.getText().toString());
            int code = MyApplication.mReadCardOptV2.at88scWriteData(startAddr, zoneFlag, data);
            if (code != 0) {
                showToast(" at88scWriteData failed");
                LogUtil.e(TAG, "at88scWriteData error,code:" + code);
                return false;
            }
            showToast(" at88scWriteData success");
            LogUtil.e(TAG, "at88scWriteData success");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /** SLE4442/4428 get remain times of verify password */
    private boolean at88scGetRemainAuthCount() {
        try {
            if (!checkInputRWFlag(edtRemainAuthRWFlag) || !checkInputZoneNo(edtRemainAuthZoneNo)) {
                return false;
            }
            int rwFlag = Integer.valueOf(edtRemainAuthRWFlag.getText().toString());
            int zoneNo = Integer.valueOf(edtRemainAuthZoneNo.getText().toString());
            int count = MyApplication.mReadCardOptV2.at88scGetRemainAuthCount(rwFlag, zoneNo);
            if (count < 0) {
                showToast(" at88scGetRemainAuthCount failed");
                LogUtil.e(TAG, "at88scGetRemainAuthCount error,code:" + count);
                return false;
            }
            String strRemain = getString(R.string.card_remain_count) + count;
            txtRemainAuthCount.setText(strRemain);
            LogUtil.e(TAG, "at88scGetRemainAuthCount success:" + strRemain);
            return true;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** Check input password */
    private boolean checkInputKey(EditText etdKey) {
        String key = etdKey.getText().toString();
        if (TextUtils.isEmpty(key) || !checkHexValue(key) || key.length() != 6) {
            showToast("key should be 6 hex characters");
            etdKey.requestFocus();
            return false;
        }
        return true;
    }

    /**
     * check input read/write flag,0-Write,1-Read
     *
     * @param edt The read/write EditText
     * @return true:success, false:fail
     */
    private boolean checkInputRWFlag(EditText edt) {
        String text = edt.getText().toString();
        if (TextUtils.isEmpty(text)) {
            showToast("RW flag should not be empty");
            edt.requestFocus();
            return false;
        }
        int rwFlag = Integer.valueOf(text);
        if (rwFlag < 0 || rwFlag > 1) {
            showToast("Read/Write flag should be 0 or 1");
            edt.requestFocus();
            return false;
        }
        return true;
    }

    /**
     * check input zone number, for AT88SC1608,there's 8 user zones which
     * Numbered 0~7
     *
     * @param edt The zone number EditText
     * @return true:success, false:fail
     */
    private boolean checkInputZoneNo(EditText edt) {
        String text = edt.getText().toString();
        if (TextUtils.isEmpty(text)) {
            showToast("Zone number should not be empty");
            edt.requestFocus();
            return false;
        }
        int zoneNo = Integer.valueOf(text);
        if (zoneNo < 0 || zoneNo > 7) {
            showToast("Zone number should in [0,7]");
            edt.requestFocus();
            return false;
        }
        return true;
    }

    /**
     * Check input zone flag,zone flag indicates which zone to read or write
     * flag:0-Configuration zone, 1-User zone
     *
     * @param edt The zone flag editText
     * @return true:success, false:fail
     */
    private boolean checkInputZoneFlag(EditText edt) {
        String text = edt.getText().toString();
        if (TextUtils.isEmpty(text)) {
            showToast("Zone flag should not be empty");
            edt.requestFocus();
            return false;
        }
        int zoneFlag = Integer.valueOf(text);
        if (zoneFlag < 0 || zoneFlag > 1) {
            showToast("Zone flag should be 0 or 1");
            edt.requestFocus();
            return false;
        }
        return true;
    }

    /** Check input start address */
    private boolean checkInputStartAddress(EditText edtStartAddress) {
        String startAddress = edtStartAddress.getText().toString();
        if (TextUtils.isEmpty(startAddress) || !checkHexValue(startAddress)) {
            showToast("startAddress should be 1~3 hex characters");
            edtStartAddress.requestFocus();
            return false;
        }
        int address = Integer.parseInt(startAddress, 16);
        if (address < 0 || address > 0x7FF) {
            showToast("startAddress should be in [000~7FF]");
            edtStartAddress.requestFocus();
            return false;
        }
        return true;
    }

    /** Check Read data input length */
    private boolean checkInputLength(EditText edtLength) {
        String length = edtLength.getText().toString();
        if (TextUtils.isEmpty(length)) {
            showToast("input length should not be empty");
            edtLength.requestFocus();
            return false;
        }
        int len = Integer.parseInt(length);
        if (len < 0 || len > 2047) {
            showToast("input length should in [0~2047]");
            edtLength.requestFocus();
            return false;
        }
        return true;
    }

    /** Check input data */
    private boolean checkInputData(EditText edtData) {
        String data = edtData.getText().toString();
        if (TextUtils.isEmpty(data) || !checkHexValue(data)) {
            showToast("input data should be hex characters");
            edtData.requestFocus();
            return false;
        }
        if (data.length() % 2 != 0) {
            showToast("illegal input data length");
            edtData.requestFocus();
            return false;
        }
        if (data.length() / 2 > 253) {
            showToast("input data should less than 253 bytes");
            edtData.requestFocus();
            return false;
        }
        return true;
    }

    /** Check hex string */
    private boolean checkHexValue(String src) {
        return Pattern.matches("[0-9a-fA-F]+", src);
    }

    /** Format strings */
    private String formatStr(String format, Object... params) {
        return String.format(Locale.getDefault(), format, params);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelCheckCard();
    }

    private void cancelCheckCard() {
        try {
            MyApplication.mReadCardOptV2.cardOff(cardType);
            MyApplication.mReadCardOptV2.cancelCheckCard();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
