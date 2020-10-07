package com.sm.sdk.demo.security;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.Constant;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sunmi.pay.hardware.aidlv2.AidlConstantsV2;
import com.sunmi.pay.hardware.aidlv2.security.SecurityOptV2;

public class DataDecryptActivity extends BaseAppCompatActivity {
    private static final String TAG = Constant.TAG;

    private EditText mEditData;
    private EditText mEditInitIV;
    private EditText mEditKeyIndex;

    private TextView mTvInfo;

    private int mDecryptType = AidlConstantsV2.Security.DATA_MODE_ECB;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_data_decrypt);
        initToolbarBringBack(R.string.security_data_decrypt);
        initView();
    }

    private void initView() {
        RadioGroup keyTypeRadioGroup = findViewById(R.id.decrypt_type_group);
        keyTypeRadioGroup.setOnCheckedChangeListener(
                (group, checkedId) -> {
                    switch (checkedId) {
                        case R.id.rb_decrypt_type1:
                            mDecryptType = AidlConstantsV2.Security.DATA_MODE_ECB;
                            break;
                        case R.id.rb_decrypt_type2:
                            mDecryptType = AidlConstantsV2.Security.DATA_MODE_CBC;
                            break;
                        case R.id.rb_decrypt_type3:
                            mDecryptType = AidlConstantsV2.Security.DATA_MODE_OFB;
                            break;
                        case R.id.rb_decrypt_type4:
                            mDecryptType = AidlConstantsV2.Security.DATA_MODE_CFB;
                            break;
                    }
                }
        );

        mEditData = findViewById(R.id.source_data);
        mEditKeyIndex = findViewById(R.id.key_index);
        mEditInitIV = findViewById(R.id.initialization_vector);

        mTvInfo = findViewById(R.id.tv_info);

        findViewById(R.id.mb_ok).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.mb_ok:
                dataDecrypt();
                break;
        }
    }

    private void dataDecrypt() {
        try {
            SecurityOptV2 securityOptV2 = MyApplication.mSecurityOptV2;

            String ivStr = mEditInitIV.getText().toString();
            String dataStr = mEditData.getText().toString();
            String keyIndexStr = mEditKeyIndex.getText().toString();

            int keyIndex;
            try {
                keyIndex = Integer.valueOf(keyIndexStr);
                if (keyIndex > 19 || keyIndex < 0) {
                    showToast(R.string.security_decrypt_index_hint);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                showToast(R.string.security_decrypt_index_hint);
                return;
            }

            if (mDecryptType != AidlConstantsV2.Security.DATA_MODE_ECB && ivStr.length() != 16) {
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
            if (mDecryptType != AidlConstantsV2.Security.DATA_MODE_ECB) {
                ivByte = ByteUtil.hexStr2Bytes(ivStr);
            } else {
                ivByte = null;
            }
            int result = securityOptV2.dataDecrypt(keyIndex, dataIn, mDecryptType, ivByte, dataOut);
            if (result == 0) {
                String hexStr = ByteUtil.bytes2HexStr(dataOut);
                LogUtil.e(TAG, "dataDecrypt output:" + hexStr);
                mTvInfo.setText(hexStr);
            } else {
                toastHint(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
