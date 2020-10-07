package com.sm.sdk.demo.security;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sunmi.pay.hardware.aidl.AidlConstants.Security;

public class DeleteKeyActivity extends BaseAppCompatActivity {
    private EditText mEditKeyIndex;
    private int keySystem = Security.SEC_MKSK;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_delete_key);
        initToolbarBringBack(R.string.security_delete_key);
        initView();
    }

    private void initView() {
        RadioGroup rdoGroup = findViewById(R.id.key_system);
        rdoGroup.setOnCheckedChangeListener(
                (group, checkedId) -> {
                    String keyIndexHint = getString(R.string.security_key_index);
                    switch (checkedId) {
                        case R.id.rb_sec_mksk:
                            keySystem = Security.SEC_MKSK;
                            mEditKeyIndex.setHint(keyIndexHint + "[0,199]");
                            break;
                        case R.id.rb_sec_dukpt:
                            keySystem = Security.SEC_DUKPT;
                            mEditKeyIndex.setHint(keyIndexHint + "[0,19]");
                            break;
                        case R.id.rb_sec_rsa_key:
                            keySystem = Security.SEC_RSA_KEY;
                            mEditKeyIndex.setHint(keyIndexHint + "[0,19]");
                            break;
                        case R.id.rb_sec_sm2_key:
                            keySystem = Security.SEC_SM2_KEY;
                            mEditKeyIndex.setHint(keyIndexHint + "[0,9]");
                            break;
                    }
                }
        );
        mEditKeyIndex = findViewById(R.id.key_index);
        findViewById(R.id.mb_ok).setOnClickListener(this);
        rdoGroup.check(R.id.rb_sec_mksk);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.mb_ok:
                deleteKey();
                break;
        }
    }

    private void deleteKey() {
        try {
            String keyIndexStr = mEditKeyIndex.getText().toString().trim();
            int keyIndex;
            try {
                keyIndex = Integer.parseInt(keyIndexStr);
                if (keySystem == Security.SEC_MKSK) {
                    if (keyIndex > 199 || keyIndex < 0) {
                        showToast("Incorrect key index,[0,199]");
                        return;
                    }
                } else if (keySystem == Security.SEC_DUKPT) {
                    if (keyIndex > 19 || keyIndex < 0) {
                        showToast(R.string.security_duKpt_key_hint);
                        return;
                    }
                } else if (keySystem == Security.SEC_RSA_KEY) {
                    if (keyIndex > 19 || keyIndex < 0) {
                        showToast("Incorrect key index,[0,19]");
                        return;
                    }
                } else if (keySystem == Security.SEC_SM2_KEY) {
                    if (keyIndex > 9 || keyIndex < 0) {
                        showToast(R.string.security_duKpt_key_hint);
                        return;
                    }
                }
            } catch (Exception e) {
                showToast("Incorrect key index");
                e.printStackTrace();
                return;
            }
            int result = MyApplication.mSecurityOptV2.deleteKey(keySystem, keyIndex);
            toastHint(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
