package com.sm.sdk.demo.security;

import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.Constant;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sunmi.pay.hardware.aidl.AidlConstants.Security;

import java.util.Random;
import java.util.regex.Pattern;

/**
 * This page show how to save TR31 KBPK(key block protection key) and
 * TR31 key, for detail info on TR31, please refer to specification
 * document "ANSI X9 TR-31_2010.pdf"
 */
public class SaveTR31KeyActivity extends BaseAppCompatActivity {
    private static final String TAG = Constant.TAG;

    private EditText edtKBPKValue;
    private EditText edtKBPKCheckValue;
    private EditText edtKBPKIndex;
    private EditText edtTR31KeyValue;
    private EditText edtTR31KBPIndex;
    private EditText edtTR31KeyIndex;
    private boolean isDukptKey;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_save_key_tr31);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.security_save_tr31_key);
        edtKBPKValue = findViewById(R.id.key_value_kbbk);
        edtKBPKCheckValue = findViewById(R.id.check_value_kbpk);
        edtKBPKIndex = findViewById(R.id.key_index_kbpk);
        edtTR31KeyValue = findViewById(R.id.key_value_tr31);
        edtTR31KBPIndex = findViewById(R.id.key_index_tr31_kbpk);
        edtTR31KeyIndex = findViewById(R.id.key_index_tr31);
        findViewById(R.id.mb_ok_kbpk).setOnClickListener(this);
        findViewById(R.id.mb_ok_tr31).setOnClickListener(this);
        RadioGroup rdoGroup = findViewById(R.id.rdo_group_key_type);
        rdoGroup.setOnCheckedChangeListener((group, checkedId) -> {
            updateDefaultData(checkedId);
        });
        this.<RadioButton>findViewById(R.id.rdo_dukpt_key).setChecked(true);
        edtKBPKIndex.setText("1");
        edtTR31KBPIndex.setText("1");
        edtTR31KeyIndex.setText("5");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mb_ok_kbpk:
                saveKBPK();
                break;
            case R.id.mb_ok_tr31:
                saveTR31Key();
                if (isDukptKey) {
                    testSavedDukptKey();
                }
                break;
        }
    }

    /** Update default kbpk and tr31 key */
    private void updateDefaultData(int checkedId) {
        isDukptKey = false;
        if (checkedId == R.id.rdo_dukpt_key) {
            isDukptKey = true;
            String[] keys = getDukptKey();
            edtKBPKValue.setText(keys[0]);
            edtTR31KeyValue.setText(keys[1]);
        } else if (checkedId == R.id.rdo_pik) {
            edtKBPKValue.setText("DD7515F2BFC17F85CE48F3CA25CB21F6");
            edtTR31KeyValue.setText("B0080P0TE00E000094B420079CC80BA3461F86FE26EFC4A3B8E4FA4C5F5341176EED7B727B8A248E");
        } else if (checkedId == R.id.rdo_other_key) {//just test pin key
            edtKBPKValue.setText("DD7515F2BFC17F85CE48F3CA25CB21F6");
            edtTR31KeyValue.setText("B0080P0TE00E000094B420079CC80BA3461F86FE26EFC4A3B8E4FA4C5F5341176EED7B727B8A248E");
        }
    }

    /** Get dukpt kbpk and tr31 key */
    private String[] getDukptKey() {
        /*we have 4 groups kbpks and tr31 keys for dukpt KSN+IPEK, group1 is not support
          group1:(BDK+KSN)
            KBPK_DATA1:35307965393068756D676F713973336B
            TR31_B0(BDK):B0104B0TX00E0100KS18FFFF9876543210E00000E79ED3F8E9B67F8E786821C8F15CE7FD7D164D1E2FE37C8EDF61959FCA8058B6
          group2:(IPEK+KSN)
            KBPK_DATA2:6D377A6A676770326D6A6D696966306E
            TR31_B1_1:B0104B1TX00E0100KS18FFFF9876543210E00000BF72B047FFD6BD3E793724A73FF533F43B1D6BC5B5D82958976545EF6F4836AB
          group3:(IPEK+KSN)
            KBPK_DATA3:6D377A6A676770326D6A6D696966306E
            TR31_B1_2:B0104B1TX00E0100KS18FFFF9876543210E00000347E67D68DF88925486D1F8969F62E69F006DACDB937AD4740F50674176BABA9
          group4:(IPEK+KSN)
            KBPK_DATA4:1D22BF32387C600AD97F9B97A51311AC
            TR31_K0:B0104K0TD12S0100KS1800604B120F92928000007E21275AAD6195EC93BC7127FB3D1FFD08AA1CF73F24C3C030DA35AC2B8C76F4
         */
        String[] kbpk = {
                "6D377A6A676770326D6A6D696966306E",
                "6D377A6A676770326D6A6D696966306E",
                "1D22BF32387C600AD97F9B97A51311AC"};
        String[] tr31key = {
                "B0104B1TX00E0100KS18FFFF9876543210E00000BF72B047FFD6BD3E793724A73FF533F43B1D6BC5B5D82958976545EF6F4836AB",
                "B0104B1TX00E0100KS18FFFF9876543210E00000347E67D68DF88925486D1F8969F62E69F006DACDB937AD4740F50674176BABA9",
                "B0104K0TD12S0100KS1800604B120F92928000007E21275AAD6195EC93BC7127FB3D1FFD08AA1CF73F24C3C030DA35AC2B8C76F4"
        };
        int index = new Random().nextInt(kbpk.length);
        return new String[]{kbpk[index], tr31key[index]};
    }

    /** Save KBPK */
    private void saveKBPK() {
        try {
            String valueStr = edtKBPKValue.getText().toString();
            String checkValueStr = edtKBPKCheckValue.getText().toString();
            String keyIndexStr = edtKBPKIndex.getText().toString();
            if (TextUtils.isEmpty(valueStr) || !checkHexValue(valueStr)) {
                String msg = "illegal kbpk key value";
                showToast(msg);
                LogUtil.e(TAG, msg);
                return;
            }
            if (!TextUtils.isEmpty(checkValueStr) && !checkHexValue(checkValueStr)) {
                String msg = "illegal kbpk key checkValue";
                showToast(msg);
                LogUtil.e(TAG, msg);
                return;
            }
            if (TextUtils.isEmpty(keyIndexStr)) {
                String msg = "illegal KBPK index";
                showToast(msg);
                LogUtil.e(TAG, msg);
                return;
            }
            byte[] keyValue = ByteUtil.hexStr2Bytes(valueStr);
            byte[] checkValue = ByteUtil.hexStr2Bytes(checkValueStr);
            int keyIndex = Integer.valueOf(keyIndexStr);
            int code = MyApplication.mSecurityOptV2.savePlaintextKey(Security.KEY_TYPE_KBPK, keyValue, checkValue, Security.KEY_ALG_TYPE_3DES, keyIndex);
            String msg = "save KBPK " + (code == 0 ? "success" : "failed");
            LogUtil.e(TAG, msg);
            showToast(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Save TR31 key */
    private void saveTR31Key() {
//        try {
//            String valueStr = edtTR31KeyValue.getText().toString();
//            String kbpkIndexStr = edtTR31KBPIndex.getText().toString();
//            String keyIndexStr = edtTR31KeyIndex.getText().toString();
//            if (TextUtils.isEmpty(valueStr)) {
//                String msg = "illegal TR31 key value";
//                showToast(msg);
//                LogUtil.e(TAG, msg);
//                return;
//            }
//            if (TextUtils.isEmpty(kbpkIndexStr)) {
//                String msg = "illegal TR31 KBPK index";
//                showToast(msg);
//                LogUtil.e(TAG, msg);
//                return;
//            }
//            if (TextUtils.isEmpty(keyIndexStr)) {
//                String msg = "illegal TR31 key index";
//                showToast(msg);
//                LogUtil.e(TAG, msg);
//                return;
//            }
//            byte[] keyValue = valueStr.getBytes();
//            int kbpkIndex = Integer.parseInt(kbpkIndexStr);
//            int keyIndex = Integer.valueOf(keyIndexStr);
//            long start = System.currentTimeMillis();
//            int code = MyApplication.mSecurityOptV2.saveTR31Key(keyValue, kbpkIndex, keyIndex);
//            long end = System.currentTimeMillis();
//            String msg = "save saveTR31Key " + (code == 0 ? "success" : "failed") + ", time:" + (end - start);
//            LogUtil.e(TAG, msg);
//            showToast(msg);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    /** check whether src is hex format */
    private boolean checkHexValue(String src) {
        return Pattern.matches("[0-9a-fA-F]+", src);
    }

    private String formatStr(String format, Object... params) {
        return String.format(format, params);
    }


    /** Test saved key by call dataEncryptDukpt() */
    private void testSavedDukptKey() {
        try {
            byte[] dataIn = ByteUtil.hexStr2Bytes("343031323334353637383930394439383700000000000000");
            int encryptType = Security.DATA_MODE_ECB;
            byte[] dataOut = new byte[dataIn.length];
            long start = System.currentTimeMillis();
            int keyIndex = Integer.valueOf(edtTR31KeyIndex.getText().toString());
            int result = MyApplication.mSecurityOptV2.dataEncryptDukpt(keyIndex, dataIn, encryptType, null, dataOut);
            long end = System.currentTimeMillis();
            if (result == 0) {
                String msg = "dukpt encrypt data success,data out:" + ByteUtil.bytes2HexStr(dataOut);
                msg += ",time:" + (end - start);
                LogUtil.e(TAG, msg);
                showToast(msg);
            } else {
                String msg = "dukpt decrypt data failed, code:" + result;
                LogUtil.e(TAG, msg);
                showToast(msg);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


}
