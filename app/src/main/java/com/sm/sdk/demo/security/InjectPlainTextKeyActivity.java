package com.sm.sdk.demo.security;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.Constant;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sunmi.pay.hardware.aidlv2.AidlConstantsV2;

public class InjectPlainTextKeyActivity extends BaseAppCompatActivity {
    private static final String TAG = Constant.TAG;

    private EditText mEditTargetPkgName;
    private EditText mEditKeyValue;
    private EditText mEditCheckValue;
    private EditText mEditKeyIndex;

    private int mKeyType = AidlConstantsV2.Security.KEY_TYPE_KEK;
    private int mKeyAlgType = AidlConstantsV2.Security.KEY_ALG_TYPE_3DES;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_save_key_plain_text);
        initToolbarBringBack(R.string.security_inject_plaintext_key);
        initView();
    }

    private void initView() {
        RadioGroup keyTypeRadioGroup = findViewById(R.id.key_type);
        keyTypeRadioGroup.setOnCheckedChangeListener(
                (group, checkedId) -> {
                    switch (checkedId) {
                        case R.id.rb_kek:
                            mKeyType = AidlConstantsV2.Security.KEY_TYPE_KEK;
                            break;
                        case R.id.rb_tmk:
                            mKeyType = AidlConstantsV2.Security.KEY_TYPE_TMK;
                            break;
                        case R.id.rb_pik:
                            mKeyType = AidlConstantsV2.Security.KEY_TYPE_PIK;
                            break;
                        case R.id.rb_tdk:
                            mKeyType = AidlConstantsV2.Security.KEY_TYPE_TDK;
                            break;
                        case R.id.rb_mak:
                            mKeyType = AidlConstantsV2.Security.KEY_TYPE_MAK;
                            break;
                        case R.id.rb_rec_key:
                            mKeyType = AidlConstantsV2.Security.KEY_TYPE_REC;
                            break;
                    }
                }
        );

        RadioGroup keyAlgTypeRadioGroup = findViewById(R.id.key_alg_type);
        keyAlgTypeRadioGroup.setOnCheckedChangeListener(
                (group, checkedId) -> {
                    switch (checkedId) {
                        case R.id.rb_3des:
                            mKeyAlgType = AidlConstantsV2.Security.KEY_ALG_TYPE_3DES;
                            break;
                        case R.id.rb_sm4:
                            mKeyAlgType = AidlConstantsV2.Security.KEY_ALG_TYPE_SM4;
                            break;
                        case R.id.rb_aes:
                            mKeyAlgType = AidlConstantsV2.Security.KEY_ALG_TYPE_AES;
                            break;
                    }
                }
        );

        mEditTargetPkgName = findViewById(R.id.target_pkg_name);
        mEditKeyValue = findViewById(R.id.key_value);
        mEditKeyIndex = findViewById(R.id.key_index);
        mEditCheckValue = findViewById(R.id.check_value);

        mEditTargetPkgName.setText("com.sunmi.sdk.demov2");
        findViewById(R.id.target_pkg_lay).setVisibility(View.VISIBLE);
        findViewById(R.id.mb_ok).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.mb_ok:
                injectPlainTextKey();
                break;
        }
    }

    private void injectPlainTextKey() {
        try {
            String targetPkgName = mEditTargetPkgName.getText().toString().trim();
            String keyValueStr = mEditKeyValue.getText().toString().trim();
            String keyIndexStr = mEditKeyIndex.getText().toString().trim();
            String checkValueStr = mEditCheckValue.getText().toString().trim();

            if (TextUtils.isEmpty(targetPkgName)) {
                showToast(R.string.security_target_pkg_name_hint);
                mEditTargetPkgName.requestFocus();
                return;
            }
            if (keyValueStr.length() == 0 || keyValueStr.length() % 8 != 0) {
                showToast(R.string.security_key_value_hint);
                return;
            }
            if (checkValueStr.length() != 0) {
                if (mKeyAlgType == AidlConstantsV2.Security.KEY_ALG_TYPE_SM4) {
                    if (checkValueStr.length() > 32 || checkValueStr.length() % 4 != 0) {
                        showToast(R.string.security_check_value_hint);
                        return;
                    }
                } else {
                    if (checkValueStr.length() > 16 || checkValueStr.length() % 4 != 0) {
                        showToast(R.string.security_check_value_hint);
                        return;
                    }
                }
            }
            int keyIndex;
            try {
                keyIndex = Integer.parseInt(keyIndexStr);
                if (keyIndex > 19 || keyIndex < 0) {
                    showToast(R.string.security_key_index_hint);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                showToast(R.string.security_key_index_hint);
                return;
            }
            byte[] keyValue = ByteUtil.hexStr2Bytes(keyValueStr);
            byte[] checkValue = ByteUtil.hexStr2Bytes(checkValueStr);
            int result = MyApplication.mSecurityOptV2.injectPlaintextKey(targetPkgName, mKeyType, keyValue, checkValue, mKeyAlgType, keyIndex);
            LogUtil.e(TAG, "injectPlainTextKey code:" + result);
            toastHint(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
