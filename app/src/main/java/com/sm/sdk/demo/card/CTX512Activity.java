package com.sm.sdk.demo.card;

import android.annotation.SuppressLint;
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
import com.sm.sdk.demo.utils.Utility;
import com.sunmi.pay.hardware.aidl.AidlConstants.CardType;
import com.sunmi.pay.hardware.aidlv2.AidlConstantsV2;
import com.sunmi.pay.hardware.aidlv2.readcard.CheckCardCallbackV2;

import java.util.Arrays;

public class CTX512Activity extends BaseAppCompatActivity {
    private static final String TAG = Constant.TAG;

    private EditText edtReadBlock;
    private EditText edtReadResult;
    private EditText edtWriteBlock;
    private EditText edtWriteData;
    private EditText edtUpdateBlock;
    private EditText edtUpdateData;
    private EditText edtGetSignBlock;
    private EditText edtGetSignRandom;
    private EditText edtGetSignResult;
    private EditText edtMultiReadBlock;
    private EditText edtMultiReadResult;

    private int cardType = CardType.CTX512B.getValue();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_ctx512);
        initView();
        checkCard();
    }

    private void initView() {
        initToolbarBringBack(R.string.card_test_ctx512);
        edtReadBlock = findViewById(R.id.edt_read_block);
        edtReadResult = findViewById(R.id.edt_read_result);
        edtWriteBlock = findViewById(R.id.edt_write_block);
        edtWriteData = findViewById(R.id.edt_write_data);
        edtUpdateBlock = findViewById(R.id.edt_upd_block);
        edtUpdateData = findViewById(R.id.edt_upd_data);

        edtGetSignBlock = findViewById(R.id.edt_get_sign_block);
        edtGetSignRandom = findViewById(R.id.edt_get_sign_random);
        edtGetSignResult = findViewById(R.id.edt_get_sign_result);

        edtMultiReadBlock = findViewById(R.id.edt_multi_read_start_block);
        edtMultiReadResult = findViewById(R.id.edt_multi_read_result);

        findViewById(R.id.mb_read).setOnClickListener(this);
        findViewById(R.id.mb_write).setOnClickListener(this);
        findViewById(R.id.mb_upd).setOnClickListener(this);
        findViewById(R.id.mb_get_sign).setOnClickListener(this);
        findViewById(R.id.mb_multi_read).setOnClickListener(this);
        RadioGroup group = findViewById(R.id.rdo_group_card_type);
        group.setOnCheckedChangeListener((group1, checkedId) -> {
            if (checkedId == R.id.rdo_ctx512B) {
                cardType = AidlConstantsV2.CardType.CTX512B.getValue();
                checkCard();
            }
        });
    }

    private void checkCard() {
        try {
            showSwingCardHintDialog();
            // Now, card type is CTX512B
            MyApplication.mReadCardOptV2.checkCard(cardType, mCheckCardCallback, 60);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private CheckCardCallbackV2 mCheckCardCallback = new CheckCardCallbackV2Wrapper() {
        @Override
        public void findMagCard(Bundle bundle) throws RemoteException {
            LogUtil.e(TAG, "findMagCard:" + Utility.bundle2String(bundle));
            dismissSwingCardHintDialog();
        }

        @Override
        public void onError(int code, String message) throws RemoteException {
            LogUtil.e(TAG, "检卡失败，code:" + code + ",msg:" + message);
            checkCard();
        }

        @Override
        public void findICCardEx(Bundle info) throws RemoteException {
            LogUtil.e(TAG, "findICCardEx:" + Utility.bundle2String(info));
            dismissSwingCardHintDialog();
        }

        @Override
        public void findRFCardEx(Bundle info) throws RemoteException {
            LogUtil.e(TAG, "findRFCardEx:" + Utility.bundle2String(info));
            dismissSwingCardHintDialog();
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mb_read:
                readBlock();
                break;
            case R.id.mb_write:
                writeBlock();
                break;
            case R.id.mb_upd:
                updateBlock();
                break;
            case R.id.mb_get_sign:
                getSignature();
                break;
            case R.id.mb_multi_read:
                multiReadBlock();
                break;
        }
    }

    /** CTX512B read block data */
    private void readBlock() {
        try {
            if (!checkInputBlock(edtReadBlock)) {
                return;
            }
            int block = Integer.valueOf(edtReadBlock.getText().toString());
            byte[] out = new byte[16];
            int len = MyApplication.mReadCardOptV2.ctx512ReadBlock(block, out);
            if (len < 0) {
                String msg = "CTX512B read block failed,code:" + len;
                LogUtil.e(TAG, msg);
                showToast(msg);
                return;
            }
            byte[] valid = Arrays.copyOf(out, len);
            String msg = ByteUtil.bytes2HexStr(valid);
            LogUtil.e(TAG, "CTX512B read block success,data:" + msg);
            edtReadResult.setText(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** CTX512B write block data */
    private void writeBlock() {
        try {
            if (!checkInputBlock(edtWriteBlock) || !checkInputData(edtWriteData, 4)) {
                return;
            }
            int block = Integer.valueOf(edtWriteBlock.getText().toString());
            byte[] data = ByteUtil.hexStr2Bytes(edtWriteData.getText().toString());
            int code = MyApplication.mReadCardOptV2.ctx512WriteBlock(block, data);
            if (code < 0) {
                String msg = "CTX512B write block failed,code:" + code;
                LogUtil.e(TAG, msg);
                showToast(msg);
            } else {
                String msg = "CTX512B write block success";
                LogUtil.e(TAG, msg);
                showToast(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** CTX512B update block data */
    private void updateBlock() {
        try {
            if (!checkInputBlock(edtUpdateBlock) || !checkInputData(edtUpdateData, 4)) {
                return;
            }
            int block = Integer.valueOf(edtUpdateBlock.getText().toString());
            byte[] data = ByteUtil.hexStr2Bytes(edtUpdateData.getText().toString());
            int code = MyApplication.mReadCardOptV2.ctx512UpdateBlock(block, data);
            if (code < 0) {
                String msg = "CTX512B update block failed,code:" + code;
                LogUtil.e(TAG, msg);
                showToast(msg);
            } else {
                String msg = "CTX512B update block success";
                LogUtil.e(TAG, msg);
                showToast(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** CTX512B get signature */
    private void getSignature() {
        try {
            if (!checkInputBlock(edtGetSignBlock) || !checkInputData(edtGetSignRandom, 12)) {
                return;
            }
            int block = Integer.valueOf(edtGetSignBlock.getText().toString());
            byte[] random = ByteUtil.hexStr2Bytes(edtGetSignRandom.getText().toString());
            byte[] out = new byte[16];
            int len = MyApplication.mReadCardOptV2.ctx512GetSignature(block, random, out);
            if (len < 0) {
                String msg = "CTX512B get signature failed,code:" + len;
                LogUtil.e(TAG, msg);
                showToast(msg);
                return;
            }
            byte[] valid = Arrays.copyOf(out, len);
            String msg = ByteUtil.bytes2HexStr(valid);
            LogUtil.e(TAG, "CTX512B get signature success,signature:" + msg);
            edtGetSignResult.setText(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** CTX512B read multi blocks */
    private void multiReadBlock() {
        try {
            if (!checkInputBlock(edtMultiReadBlock)) {
                return;
            }
            int startBlock = Integer.valueOf(edtMultiReadBlock.getText().toString());
            byte[] out = new byte[16];
            int len = MyApplication.mReadCardOptV2.ctx512MultiReadBlock(startBlock, out);
            if (len < 0) {
                String msg = "CTX512B multi read blocks failed,code:" + len;
                LogUtil.e(TAG, msg);
                showToast(msg);
                return;
            }
            byte[] valid = Arrays.copyOf(out, len);
            String msg = ByteUtil.bytes2HexStr(valid);
            LogUtil.e(TAG, "CTX512B multi read blocks success,data:" + msg);
            edtMultiReadResult.setText(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Check input block */
    private boolean checkInputBlock(EditText edtText) {
        CharSequence text = edtText.getText();
        if (TextUtils.isEmpty(text)) {
            showToast("block should be in [0~31]");
            edtText.requestFocus();
            return false;
        }
        return true;
    }

    /** Check input data */
    @SuppressLint("DefaultLocale")
    private boolean checkInputData(EditText edtText, int len) {
        CharSequence text = edtText.getText();
        if (TextUtils.isEmpty(text) || text.length() != len) {
            showToast(String.format("input data should be %d hex characters", len));
            edtText.requestFocus();
            return false;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelCheckCard();
    }

    private void cancelCheckCard() {
        try {
            MyApplication.mReadCardOptV2.cardOff(CardType.CTX512B.getValue());
            MyApplication.mReadCardOptV2.cancelCheckCard();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
