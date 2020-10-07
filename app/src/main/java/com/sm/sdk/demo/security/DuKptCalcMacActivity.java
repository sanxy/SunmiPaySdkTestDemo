package com.sm.sdk.demo.security;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.Constant;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sunmi.pay.hardware.aidl.AidlConstants.Security;

import java.util.Arrays;

public class DuKptCalcMacActivity extends BaseAppCompatActivity {
    private static final String TAG = Constant.TAG;

    private EditText mEditData;
    private EditText mEditKeyIndex;
    private EditText mMacData;
    private TextView mTvInfo;
    private RadioGroup mMacOptGroup;
    private int mCalcType = Security.MAC_ALG_X9_19;
    private int mKeySelect = Security.DUKPT_KEY_SELECT_KEY_MAC_BOTH;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_dukpt_calc_mac);
        initToolbarBringBack(R.string.security_DuKpt_calc_mac);
        initView();
    }

    private void initView() {
        final LinearLayout macDataLay = findViewById(R.id.mac_data_lay);
        mMacOptGroup = findViewById(R.id.mac_opt_group);
        mMacOptGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.rb_mac_opt_calc:
                    macDataLay.setVisibility(View.GONE);
                    break;
                case R.id.rb_mac_opt_verify:
                    macDataLay.setVisibility(View.VISIBLE);
                    break;
            }
        });
        mMacOptGroup.check(R.id.rb_mac_opt_calc);
        RadioGroup keySelectGroup = findViewById(R.id.key_select_group);
        keySelectGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.rb_key_select_data_both:
                    mKeySelect = Security.DUKPT_KEY_SELECT_KEY_MAC_BOTH;
                    break;
                case R.id.rb_key_select_data_rsp:
                    mKeySelect = Security.DUKPT_KEY_SELECT_KEY_MAC_RSP;
                    break;
            }
        });

        RadioGroup macTypeGroup = findViewById(R.id.mac_type);
        macTypeGroup.setOnCheckedChangeListener(
                (group, checkedId) -> {
                    switch (checkedId) {
                        case R.id.rb_mac_type1:
                            mCalcType = Security.MAC_ALG_X9_19;
                            break;
                        case R.id.rb_mac_type2:
                            mCalcType = Security.MAC_ALG_FAST_MODE_INTERNATIONAL;
                            break;
                        case R.id.rb_mac_type3:
                            mCalcType = Security.MAC_ALG_CBC_INTERNATIONAL;
                            break;
                    }
                }
        );

        mEditData = findViewById(R.id.source_data);
        mEditKeyIndex = findViewById(R.id.key_index);
        mMacData = findViewById(R.id.mac_data);
        mTvInfo = findViewById(R.id.tv_info);
        mEditData.setText("123456789012345678901234");
        findViewById(R.id.mb_ok).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.mb_ok:
                onOKButtonClick();
                break;
        }
    }

    private void onOKButtonClick() {
        if (mMacOptGroup.getCheckedRadioButtonId() == R.id.rb_mac_opt_calc) {
            calcMac();
        } else if (mMacOptGroup.getCheckedRadioButtonId() == R.id.rb_mac_opt_verify) {
            verifyMac();
        }
    }

    /** Calculate Mac */
    private void calcMac() {
        try {
            String dataStr = mEditData.getText().toString();
            String keyIndexStr = mEditKeyIndex.getText().toString();
            if (TextUtils.isEmpty(dataStr)) {
                showToast(R.string.security_source_data_hint);
                return;
            }
            if (TextUtils.isEmpty(keyIndexStr)) {
                showToast(R.string.security_duKpt_key_hint);
                return;
            }
            int keyIndex = Integer.valueOf(keyIndexStr);
            if (keyIndex < 0 || keyIndex > 19) {
                showToast(R.string.security_duKpt_key_hint);
                return;
            }
            byte[] dataOut = new byte[16];
            byte[] dataIn = ByteUtil.hexStr2Bytes(dataStr);
            int code = MyApplication.mSecurityOptV2.calcMacDukptEx(mKeySelect, keyIndex, mCalcType, dataIn, dataOut);
            if (code == 0) {
                String macStr = null;
                byte[] rear = Arrays.copyOfRange(dataOut, 8, dataOut.length);
                if (Arrays.equals(rear, new byte[8])) {//last 8 bytes all 0, 3DES dukpt, Mac is the first 8 bytes
                    macStr = ByteUtil.bytes2HexStr(Arrays.copyOf(dataOut, 8));
                } else {// Dukpt-AES, Mac is 16 bytes
                    macStr = ByteUtil.bytes2HexStr(dataOut);
                }
                mTvInfo.setText(macStr);
            } else {
                toastHint(code);
            }
            LogUtil.e(TAG, "calculate MAC dukpt,dataOut:" + ByteUtil.bytes2HexStr(dataOut));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Verify Mac */
    private void verifyMac() {
        try {
            String sourceDataStr = mEditData.getText().toString();
            String macDataStr = mMacData.getText().toString();
            String keyIndexStr = mEditKeyIndex.getText().toString();
            if (TextUtils.isEmpty(sourceDataStr) | TextUtils.isEmpty(macDataStr)) {
                showToast(R.string.security_source_data_hint);
                return;
            }
            if (TextUtils.isEmpty(keyIndexStr)) {
                showToast(R.string.security_duKpt_key_hint);
                return;
            }
            int keyIndex = Integer.valueOf(keyIndexStr);
            if (keyIndex < 0 || keyIndex > 19) {
                showToast(R.string.security_duKpt_key_hint);
                return;
            }
            byte[] sourceData = ByteUtil.hexStr2Bytes(sourceDataStr);
            byte[] macData = ByteUtil.hexStr2Bytes(macDataStr);
            int code = MyApplication.mSecurityOptV2.verifyMacDukptEx(mKeySelect, keyIndex, mCalcType, sourceData, macData);
            mTvInfo.setText(code == 0 ? getString(R.string.success) : getString(R.string.fail));
            LogUtil.e(TAG, "verifyMac code:" + code);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
