package com.sm.sdk.demo.security;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sunmi.pay.hardware.aidl.AidlConstants.Security;

public class DuKptDataEncryptActivity extends BaseAppCompatActivity {

    private EditText mEditData;
    private EditText mEditInitIV;
    private EditText mEditKeyIndex;
    private TextView mTvInfo;
    private int mEncryptType = Security.DATA_MODE_ECB;
    private int mKeySelect = Security.DUKPT_KEY_SELECT_KEY_DATA_BOTH;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_data_encrypt);
        initToolbarBringBack(R.string.security_DuKpt_data_encrypt);
        initView();
    }

    private void initView() {
        findViewById(R.id.key_select_lay).setVisibility(View.VISIBLE);
        RadioGroup keySelectGroup = findViewById(R.id.key_select_group);
        keySelectGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.rb_key_select_data_both:
                    mKeySelect = Security.DUKPT_KEY_SELECT_KEY_DATA_BOTH;
                    break;
                case R.id.rb_key_select_data_rsp:
                    mKeySelect = Security.DUKPT_KEY_SELECT_KEY_DATA_RSP;
                    break;
            }
        });
        RadioGroup encTypeGroup = findViewById(R.id.encrypt_type_group);
        encTypeGroup.setOnCheckedChangeListener((group, checkedId) -> {
                    switch (checkedId) {
                        case R.id.rb_encrypt_type1:
                            mEncryptType = Security.DATA_MODE_ECB;
                            break;
                        case R.id.rb_encrypt_type2:
                            mEncryptType = Security.DATA_MODE_CBC;
                            break;
                        case R.id.rb_encrypt_type3:
                            mEncryptType = Security.DATA_MODE_OFB;
                            break;
                        case R.id.rb_encrypt_type4:
                            mEncryptType = Security.DATA_MODE_CFB;
                            break;
                    }
                }
        );
        mEditData = findViewById(R.id.source_data);
        mEditKeyIndex = findViewById(R.id.key_index);
        mEditInitIV = findViewById(R.id.initialization_vector);
        mTvInfo = findViewById(R.id.tv_info);
        mEditData.setText("343031323334353637383930394439383700000000000000");
        findViewById(R.id.mb_ok).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.mb_ok:
                dataEncrypt();
                break;
        }
    }

    private void dataEncrypt() {
        try {
            String ivStr = mEditInitIV.getText().toString();
            String dataStr = mEditData.getText().toString();
            String keyIndexStr = mEditKeyIndex.getText().toString();
            int keyIndex;
            try {
                keyIndex = Integer.valueOf(keyIndexStr);
                if (keyIndex < 0 || keyIndex > 19) {
                    showToast(R.string.security_duKpt_key_hint);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                showToast(R.string.security_duKpt_key_hint);
                return;
            }
            if (mEncryptType != Security.DATA_MODE_ECB && ivStr.length() != 16) {
                showToast(R.string.security_init_vector_hint);
                return;
            }
            if (dataStr.trim().length() == 0 || dataStr.length() % 16 != 0) {
                showToast(R.string.security_source_data_hint);
                return;
            }
            byte[] dataIn = ByteUtil.hexStr2Bytes(dataStr);
            byte[] dataOut = new byte[dataIn.length];
            byte[] ivByte;
            if (mEncryptType != Security.DATA_MODE_ECB) {
                ivByte = ByteUtil.hexStr2Bytes(ivStr);
            } else {
                ivByte = null;
            }
            int result = MyApplication.mSecurityOptV2.dataEncryptDukptEx(mKeySelect, keyIndex, dataIn, mEncryptType, ivByte, dataOut);
            if (result == 0) {
                String hexStr = ByteUtil.bytes2HexStr(dataOut);
                mTvInfo.setText(hexStr);
            } else {
                toastHint(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
