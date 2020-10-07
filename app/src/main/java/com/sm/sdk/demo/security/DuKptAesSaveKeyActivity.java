package com.sm.sdk.demo.security;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputLayout;
import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.DesAesUtil;
import com.sunmi.pay.hardware.aidlv2.AidlConstantsV2;

public class DuKptAesSaveKeyActivity extends BaseAppCompatActivity {
    private static final String TAG = "DuKptAesSaveKeyActivity";
    private static final String KEY128 = "FEDCBA9876543210F1F1F1F1F1F1F1F1";
    private static final String KEY192 = "FEDCBA9876543210F1F1F1F1F1F1F1F1F1F1F1F1F1F1F1F1";
    private static final String KEY256 = "76543210F1F1F1F1F1F1F1F1FEDCBA9876543210F1F1F1F1F1F1F1F1F1F1F1F1";

    private TextInputLayout keyIndicator;
    private EditText mEditKSN;
    private EditText mEditKeyIndex;
    private EditText mEditKeyValue;
    private EditText mEditCheckValue;
    private View checkValueLay;

    private int mDukptKeyType = AidlConstantsV2.Security.DUKPT_KEY_TYPE_AES128;
    private int mKeyType = AidlConstantsV2.Security.KEY_TYPE_DUPKT_IPEK;
    private int mKeyAlgType = AidlConstantsV2.Security.KEY_ALG_TYPE_AES;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_dukpt_aes_save_key);
        initToolbarBringBack(R.string.security_DuKpt_aes_save_key);
        initView();
    }

    private void initView() {
        RadioGroup rdoGrpDukptKeyType = findViewById(R.id.rdo_dukpt_key_type);
        rdoGrpDukptKeyType.setOnCheckedChangeListener(
                (group, checkedId) -> {
                    switch (checkedId) {
                        case R.id.rb_aes128:
                            mDukptKeyType = AidlConstantsV2.Security.DUKPT_KEY_TYPE_AES128;
                            keyIndicator.setCounterMaxLength(32);
                            mEditKeyValue.setText(KEY128);
                            mEditCheckValue.setText(getCheckValue(KEY128));
                            break;
                        case R.id.rb_aes192:
                            mDukptKeyType = AidlConstantsV2.Security.DUKPT_KEY_TYPE_AES192;
                            keyIndicator.setCounterMaxLength(48);
                            mEditKeyValue.setText(KEY192);
                            mEditCheckValue.setText(getCheckValue(KEY192));
                            break;
                        case R.id.rb_aes256:
                            mDukptKeyType = AidlConstantsV2.Security.DUKPT_KEY_TYPE_AES256;
                            keyIndicator.setCounterMaxLength(64);
                            mEditKeyValue.setText(KEY256);
                            mEditCheckValue.setText(getCheckValue(KEY256));
                            break;
                    }
                });

        RadioGroup rdoGrpKeyType = findViewById(R.id.key_type);
        rdoGrpKeyType.setOnCheckedChangeListener(
                (group, checkedId) -> {
                    switch (checkedId) {
                        case R.id.rb_ipek:
                            mKeyType = AidlConstantsV2.Security.KEY_TYPE_DUPKT_IPEK;
                            break;
                        case R.id.rb_bdk:
                            mKeyType = AidlConstantsV2.Security.KEY_TYPE_DUPKT_BDK;
                            break;
                    }
                }
        );

        RadioGroup rdoGrpKeyAlgType = findViewById(R.id.key_alg_type);
        rdoGrpKeyAlgType.setOnCheckedChangeListener(
                (group, checkedId) -> {
                    switch (checkedId) {
                        case R.id.rb_aes:
                            mKeyAlgType = AidlConstantsV2.Security.KEY_ALG_TYPE_AES;
                            break;
                    }
                }
        );
        mEditKSN = findViewById(R.id.ksn);
        mEditKeyIndex = findViewById(R.id.key_index);
        mEditKeyValue = findViewById(R.id.key_value);
        mEditCheckValue = findViewById(R.id.check_value);
        keyIndicator = findViewById(R.id.key_indicator);
        checkValueLay = findViewById(R.id.check_value_lay);
        findViewById(R.id.mb_ok).setOnClickListener(this);
        rdoGrpDukptKeyType.check(R.id.rb_aes128);
        rdoGrpKeyType.check(R.id.rb_ipek);

        mEditKeyIndex.setHint(getString(R.string.security_key_index) + "(10~19)");
        mEditKSN.setText("12345678901234560000");
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.mb_ok:
                saveDukptAesKey();
                break;
        }
    }

    private void saveDukptAesKey() {
        try {
            String keyValueStr = mEditKeyValue.getText().toString().trim();
            String ksnStr = mEditKSN.getText().toString().trim();
            String keyIndexStr = mEditKeyIndex.getText().toString().trim();
            String checkValueStr = mEditCheckValue.getText().toString();

            if (keyValueStr.length() == 0 || keyValueStr.length() % 8 != 0) {
                showToast(R.string.security_key_value_hint);
                return;
            }
            if (ksnStr.length() != 20) {
                showToast(R.string.security_ksn_hint);
                return;
            }
            if (!TextUtils.isEmpty(checkValueStr) && checkValueStr.length() != 32) {
                showToast(R.string.security_checkvalue_length_error);
                return;
            }
            int keyIndex;
            try {
                keyIndex = Integer.parseInt(keyIndexStr);
                if (keyIndex < 10 || keyIndex > 19) {
                    showToast(R.string.security_duKpt_key_hint);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                showToast(R.string.security_duKpt_key_hint);
                return;
            }
            byte[] ksnBytes = ByteUtil.hexStr2Bytes(ksnStr);
            byte[] keyValue = ByteUtil.hexStr2Bytes(keyValueStr);
            byte[] checkValue = null;
            if (!TextUtils.isEmpty(checkValueStr)) {
                checkValue = ByteUtil.hexStr2Bytes(checkValueStr);
            }
            int result = MyApplication.mSecurityOptV2.saveKeyDukptAES(mDukptKeyType, mKeyType, keyValue, checkValue, ksnBytes, mKeyAlgType, keyIndex);
            toastHint(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getCheckValue(String key) {
        byte[] keyData = ByteUtil.hexStr2Bytes(key);
        byte[] data = new byte[16];
        byte[] value = DesAesUtil.aseEncrypt(keyData, data);
        return ByteUtil.bytes2HexStr(value);
    }
}
