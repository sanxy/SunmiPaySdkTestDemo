package com.sm.sdk.demo.security;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sunmi.pay.hardware.aidlv2.security.SecurityOptV2;

public class DuKptKSNOperateActivity extends BaseAppCompatActivity {

    private TextView mTvInfo;

    private EditText mEditKeyIndex;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_dukpt_ksn);
        initToolbarBringBack(R.string.security_duKpt_ksn_control);
        initView();
    }

    private void initView() {
        mEditKeyIndex = findViewById(R.id.key_index);
        mTvInfo = findViewById(R.id.tv_info);

        findViewById(R.id.mb_get_ksn).setOnClickListener(this);
        findViewById(R.id.mb_ksn_increased).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.mb_ksn_increased:
                ksnIncreased();
                break;
            case R.id.mb_get_ksn:
                getKsn();
                break;
        }
    }

    private void ksnIncreased() {
        try {
            SecurityOptV2 securityOptV2 = MyApplication.mSecurityOptV2;
            String keyIndexStr = mEditKeyIndex.getText().toString();
            int keyIndex;
            try {
                keyIndex = Integer.parseInt(keyIndexStr);
                if (keyIndex < 0 || keyIndex > 19) {
                    showToast(R.string.security_duKpt_key_hint);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                showToast(R.string.security_duKpt_key_hint);
                return;
            }
            int result = securityOptV2.dukptIncreaseKSN(keyIndex);
            toastHint(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getKsn() {
        try {
            SecurityOptV2 securityOptV2 = MyApplication.mSecurityOptV2;
            String keyIndexStr = mEditKeyIndex.getText().toString();
            //Nornmal dukpt key index is 0~9
            //Dukpt-AES key index is 10~19
            int keyIndex;
            try {
                keyIndex = Integer.parseInt(keyIndexStr);
                if (keyIndex < 0 || keyIndex > 19) {
                    showToast(R.string.security_duKpt_key_hint);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                showToast(R.string.security_duKpt_key_hint);
                return;
            }
            int len = 10;//Nornmal dukpt KSN length is 10
            if (keyIndex >= 10) {//Dukpt-AES KSN length is 12
                len = 12;
            }
            byte[] dataOut = new byte[len];
            int result = securityOptV2.dukptCurrentKSN(keyIndex, dataOut);
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
