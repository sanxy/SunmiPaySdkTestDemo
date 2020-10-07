package com.sm.sdk.demo.security;

import android.os.Bundle;
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
import com.sm.sdk.demo.utils.ByteUtil;
import com.sunmi.pay.hardware.aidlv2.AidlConstantsV2;

public class CalcMacActivity extends BaseAppCompatActivity {
    private static final String TAG = Constant.TAG;

    private EditText mEditData;
    private EditText mEditKeyIndex;
    private TextView mTvInfo;
    private int mCalcType = AidlConstantsV2.Security.MAC_ALG_ISO_9797_1_MAC_ALG1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_calc_mac);
        initToolbarBringBack(R.string.security_calc_mac);
        initView();
    }

    private void initView() {
        RadioGroup keyTypeRadioGroup = findViewById(R.id.mac_type);
        keyTypeRadioGroup.setOnCheckedChangeListener(
                (group, checkedId) -> {
                    switch (checkedId) {
                        case R.id.rb_mac_type1:
                            mCalcType = AidlConstantsV2.Security.MAC_ALG_ISO_9797_1_MAC_ALG1;
                            break;
                        case R.id.rb_mac_type2:
                            mCalcType = AidlConstantsV2.Security.MAC_ALG_ISO_9797_1_MAC_ALG3;
                            break;
                        case R.id.rb_mac_type3:
                            mCalcType = AidlConstantsV2.Security.MAC_ALG_ISO_16609_MAC_ALG1;
                            break;
                        case R.id.rb_mac_type4:
                            mCalcType = AidlConstantsV2.Security.MAC_ALG_FAST_MODE;
                            break;
                        case R.id.rb_mac_type5:
                            mCalcType = AidlConstantsV2.Security.MAC_ALG_X9_19;
                            break;
                        case R.id.rb_mac_type6:
                            mCalcType = AidlConstantsV2.Security.MAC_ALG_CBC;
                            break;
                        case R.id.rb_mac_type7:
                            mCalcType = AidlConstantsV2.Security.MAC_ALG_CUP_SM4_MAC_ALG1;
                            break;
                    }
                }
        );

        mEditData = findViewById(R.id.source_data);
        mEditKeyIndex = findViewById(R.id.key_index);
        mTvInfo = findViewById(R.id.tv_info);
        findViewById(R.id.mb_ok).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.mb_ok:
                calcMac();
                break;
        }
    }

    private void calcMac() {
        try {
            String dataStr = mEditData.getText().toString();
            String keyIndexStr = mEditKeyIndex.getText().toString();
            if (TextUtils.isEmpty(dataStr) || dataStr.length() % 2 != 0) {
                showToast(R.string.security_source_data_hint);
                return;
            }
            if (TextUtils.isEmpty(keyIndexStr)) {
                showToast(R.string.security_key_index_hint);
                return;
            }
            int keyIndex = Integer.valueOf(keyIndexStr);
            if (keyIndex < 0 || keyIndex > 19) {
                showToast(R.string.security_key_index_hint);
                return;
            }
            byte[] dataOut = new byte[8];
            if (mCalcType == AidlConstantsV2.Security.MAC_ALG_CUP_SM4_MAC_ALG1) {
                dataOut = new byte[16];
            }
            byte[] dataBytes = ByteUtil.hexStr2Bytes(dataStr);
            int result = MyApplication.mSecurityOptV2.calcMac(keyIndex, mCalcType, dataBytes, dataOut);
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
