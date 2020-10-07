package com.sm.sdk.demo.card;

import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;

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
import java.util.regex.Pattern;

public class AT24CActivity extends BaseAppCompatActivity {
    private EditText edtReadStartAddr;
    private EditText edtReadLen;
    private EditText edtReadResult;
    private EditText edtWriteStartAddr;
    private EditText edtWriteData;

    private int cardType = AidlConstantsV2.CardType.AT24C01.getValue();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_at24c);
        initView();
        checkCard();
    }

    private void initView() {
        initToolbarBringBack(R.string.card_test_at24c);
        edtReadStartAddr = findViewById(R.id.edt_read_data_start_address);
        edtReadLen = findViewById(R.id.edt_read_data_len);
        edtReadResult = findViewById(R.id.edt_read_data_result);
        edtWriteStartAddr = findViewById(R.id.edt_write_start_address);
        edtWriteData = findViewById(R.id.edt_write_data);

        findViewById(R.id.mb_read_data).setOnClickListener(this);
        findViewById(R.id.mb_write_data).setOnClickListener(this);
        RadioGroup group = findViewById(R.id.rdo_group_card_type);
        group.setOnCheckedChangeListener((group1, checkedId) -> {
            switch (checkedId) {
                case R.id.rdo_at24c_01:
                    cardType = AidlConstantsV2.CardType.AT24C01.getValue();
                    checkCard();
                    break;
                case R.id.rdo_at24c_02:
                    cardType = AidlConstantsV2.CardType.AT24C02.getValue();
                    checkCard();
                    break;
                case R.id.rdo_at24c_04:
                    cardType = AidlConstantsV2.CardType.AT24C04.getValue();
                    checkCard();
                    break;
                case R.id.rdo_at24c_08:
                    cardType = AidlConstantsV2.CardType.AT24C08.getValue();
                    checkCard();
                    break;
                case R.id.rdo_at24c_16:
                    cardType = AidlConstantsV2.CardType.AT24C16.getValue();
                    checkCard();
                    break;
                case R.id.rdo_at24c_32:
                    cardType = AidlConstantsV2.CardType.AT24C32.getValue();
                    checkCard();
                    break;
                case R.id.rdo_at24c_64:
                    cardType = AidlConstantsV2.CardType.AT24C64.getValue();
                    checkCard();
                    break;
                case R.id.rdo_at24c_128:
                    cardType = AidlConstantsV2.CardType.AT24C128.getValue();
                    checkCard();
                    break;
                case R.id.rdo_at24c_256:
                    cardType = AidlConstantsV2.CardType.AT24C256.getValue();
                    checkCard();
                    break;
                case R.id.rdo_at24c_512:
                    cardType = AidlConstantsV2.CardType.AT24C512.getValue();
                    checkCard();
                    break;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mb_read_data:
                at24cReadData();
                break;
            case R.id.mb_write_data:
                at24cWriteData();
                break;
        }
    }

    private void checkCard() {
        try {
            showSwingCardHintDialog();
            MyApplication.mReadCardOptV2.checkCard(cardType, mCheckCardCallback, 60);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private CheckCardCallbackV2 mCheckCardCallback = new CheckCardCallbackV2Wrapper() {

        @Override
        public void findMagCard(Bundle bundle) throws RemoteException {
            LogUtil.e(Constant.TAG, "findMagCard");
            dismissSwingCardHintDialog();
        }

        @Override
        public void findICCard(String atr) throws RemoteException {
            LogUtil.e(Constant.TAG, "findICCard:" + atr);
            dismissSwingCardHintDialog();
        }

        @Override
        public void findRFCard(String uuid) throws RemoteException {
            LogUtil.e(Constant.TAG, "findRFCard:" + uuid);
            dismissSwingCardHintDialog();
        }

        @Override
        public void onError(int code, String message) throws RemoteException {
            checkCard();
        }
    };


    /** AT24C card read data */
    private boolean at24cReadData() {
        try {
            if (!checkInputStartAddress(edtReadStartAddr) || !checkInputLength(edtReadLen)) {
                return false;
            }
            String startAddress = edtReadStartAddr.getText().toString();
            String length = edtReadLen.getText().toString();
            int startAddr = Integer.parseInt(startAddress, 16);
            int len = Integer.parseInt(length);
            byte[] out = new byte[260];
            int retLen = MyApplication.mReadCardOptV2.at24cReadData(startAddr, len, out);
            if (retLen < 0) {
                showToast(" at24cReadData failed");
                LogUtil.e(Constant.TAG, "at24cReadData failed,code:" + retLen);
                return false;
            }
            String data = ByteUtil.bytes2HexStr(Arrays.copyOf(out, retLen));
            edtReadResult.setText(data);
            LogUtil.e(Constant.TAG, "at24cReadData success,data:" + data);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /** AT24C card write data */
    private boolean at24cWriteData() {
        try {
            if (!checkInputStartAddress(edtWriteStartAddr)
                    || !checkInputData(edtWriteData)) {
                return false;
            }
            String startAddress = edtWriteStartAddr.getText().toString();
            String writeData = edtWriteData.getText().toString();
            int startAddr = Integer.parseInt(startAddress, 16);
            byte[] data = ByteUtil.hexStr2Bytes(writeData);
            int code = MyApplication.mReadCardOptV2.at24cWriteData(startAddr, data);
            if (code != 0) {
                showToast(" at24cWriteData failed");
                LogUtil.e(Constant.TAG, "at24cWriteData error,code:" + code);
                return false;
            }
            showToast(" at24cWriteData success");
            LogUtil.e(Constant.TAG, "at24cWriteData success");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
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
        if (address < 0 || address > 0x3FF) {
            showToast("startAddress should be in [000~3FF]");
            edtStartAddress.requestFocus();
            return false;
        }
        return true;
    }

    /** Check input length */
    private boolean checkInputLength(EditText edtLength) {
        String length = edtLength.getText().toString();
        if (TextUtils.isEmpty(length)) {
            showToast("startAddress should in [0~1024]");
            edtLength.requestFocus();
            return false;
        }
        int len = Integer.parseInt(length);
        if (len < 0 || len > 1024) {
            showToast("startAddress should  in [0~1024]");
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
        if (data.length() > 253) {
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
