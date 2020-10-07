package com.sm.sdk.demo.card;

import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.Constant;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.card.wrapper.CheckCardCallbackV2Wrapper;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sunmi.pay.hardware.aidlv2.AidlConstantsV2;
import com.sunmi.pay.hardware.aidlv2.readcard.CheckCardCallbackV2;

import java.util.Arrays;
import java.util.regex.Pattern;

public class MifarePlusActivity extends BaseAppCompatActivity {
    private EditText edtReadBlockNo;
    private EditText edtReadKey;
    private EditText edtReadBlockData;
    private EditText edtWriteBlockNo;
    private EditText edtWriteKey;
    private EditText edtWriteBlockData;
    private EditText edtAlterBlockNo;
    private EditText edtAlterOldKey;
    private EditText edtAlterNewKey;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_mifareplus);
        initView();
        checkCard();
    }

    private void initView() {
        initToolbarBringBack(R.string.card_test_mifare_plus);
        edtReadBlockNo = findViewById(R.id.edt_read_block_no);
        edtReadKey = findViewById(R.id.edt_read_key);
        edtReadBlockData = findViewById(R.id.edt_read_block_data);
        edtWriteBlockNo = findViewById(R.id.edt_write_block_no);
        edtWriteKey = findViewById(R.id.edt_write_key);
        edtWriteBlockData = findViewById(R.id.edt_write_block_data);
        edtAlterBlockNo = findViewById(R.id.edt_alter_block_no);
        edtAlterOldKey = findViewById(R.id.edt_alter_old_key);
        edtAlterNewKey = findViewById(R.id.edt_alter_new_key);
        findViewById(R.id.mb_read).setOnClickListener(this);
        findViewById(R.id.mb_write).setOnClickListener(this);
        findViewById(R.id.mb_change_password).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mb_read:
                mifarePlusReadBlock();
                break;
            case R.id.mb_write:
                mifarePlusWriteBlockData();
                break;
            case R.id.mb_change_password:
                mifarePlusChangeBlockKey();
                break;
        }
    }

    private void checkCard() {
        try {
            showSwingCardHintDialog();
            MyApplication.mReadCardOptV2.checkCard(AidlConstantsV2.CardType.MIFARE_PLUS.getValue(), mCheckCardCallback, 60);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private CheckCardCallbackV2 mCheckCardCallback = new CheckCardCallbackV2Wrapper() {

        @Override
        public void findMagCard(Bundle bundle) throws RemoteException {
            LogUtil.e(Constant.TAG, "findMagCard");
            dismissSwingCardHintDialog();
        }

        @Override
        public void findICCard(String atr) throws RemoteException {
            LogUtil.e(Constant.TAG, "findICCard:" + atr);
            dismissSwingCardHintDialog();
        }

        @Override
        public void findRFCard(String uuid) throws RemoteException {
            LogUtil.e(Constant.TAG, "findRFCard:" + uuid);
            dismissSwingCardHintDialog();
        }

        @Override
        public void onError(int code, String message) throws RemoteException {
            checkCard();
        }
    };

    /** MifarePlus read block data */
    private void mifarePlusReadBlock() {
        try {
            if (!checkInput(edtReadBlockNo, edtReadKey, null, null)) {
                return;
            }
            String blockNo = edtReadBlockNo.getText().toString();
            String blockKey = edtReadKey.getText().toString();
            int blkNo = Integer.parseInt(blockNo, 16);
            byte[] key = ByteUtil.hexStr2Bytes(blockKey);
            byte[] out = new byte[260];
            int len = MyApplication.mReadCardOptV2.mifarePlusReadBlock(blkNo, key, out);
            if (len < 0) {
                showToast("mifarePlusReadBlock failed");
                LogUtil.e(Constant.TAG, "mifarePlusReadBlock error,code:" + len);
                return;
            }
            byte[] valid = Arrays.copyOf(out, len);
            edtReadBlockData.setText(ByteUtil.bytes2HexStr(valid));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** MifarePlus write block data */
    private void mifarePlusWriteBlockData() {
        try {
            if (!checkInput(edtWriteBlockNo, edtWriteKey, edtWriteBlockData, null)) {
                return;
            }
            String blockNo = edtWriteBlockNo.getText().toString();
            String blockKey = edtWriteKey.getText().toString();
            String blockData = edtWriteBlockData.getText().toString();
            int blkNo = Integer.parseInt(blockNo, 16);
            byte[] key = ByteUtil.hexStr2Bytes(blockKey);
            byte[] data = ByteUtil.hexStr2Bytes(blockData);
            int code = MyApplication.mReadCardOptV2.mifarePlusWriteBlock(blkNo, key, data);
            if (code < 0) {
                showToast("mifarePlusWriteBlockData failed");
                LogUtil.e(Constant.TAG, "mifarePlusWriteBlockData error,code:" + code);
            } else {
                showToast("mifarePlusWriteBlockData success");
                LogUtil.e(Constant.TAG, "mifarePlusWriteBlockData success");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** MifarePlus change block key */
    private void mifarePlusChangeBlockKey() {
        try {
            if (!checkInput(edtAlterBlockNo, edtAlterOldKey, null, edtAlterNewKey)) {
                return;
            }
            String blockNo = edtAlterBlockNo.getText().toString();
            String blockOldKey = edtAlterOldKey.getText().toString();
            String blockNewkey = edtAlterNewKey.getText().toString();
            int blkNo = Integer.parseInt(blockNo, 16);
            byte[] oldKey = ByteUtil.hexStr2Bytes(blockOldKey);
            byte[] newKey = ByteUtil.hexStr2Bytes(blockNewkey);
            int code = MyApplication.mReadCardOptV2.mifarePlusChangeBlockKey(blkNo, oldKey, newKey);
            if (code < 0) {
                showToast("mifarePlusChangeBlockKey failed");
                LogUtil.e(Constant.TAG, "mifarePlusChangeBlockKey error,code:" + code);
            } else {
                showToast("mifarePlusChangeBlockKey success");
                LogUtil.e(Constant.TAG, "mifarePlusChangeBlockKey success");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Check input data */
    private boolean checkInput(EditText block, EditText key1, EditText data, EditText key2) {
        if (block != null) {
            String blockNo = block.getText().toString();
            if (TextUtils.isEmpty(blockNo) || !checkHexValue(blockNo)) {
                showToast("blockNo should be 2 hex characters!");
                block.requestFocus();
                return false;
            }
            int blkNo = Integer.parseInt(blockNo, 16);
            if (blkNo < 0 || blkNo >= 0x100) {
                showToast("blockNo should in [00~FF]");
                block.requestFocus();
                return false;
            }
        }
        if (key1 != null) {
            String blockKey = key1.getText().toString();
            if (TextUtils.isEmpty(blockKey) || !checkHexValue(blockKey) || blockKey.length() != 32) {
                showToast("blockKey should be 32 hex characters!");
                key1.requestFocus();
                return false;
            }
        }
        if (data != null) {
            String blockData = data.getText().toString();
            if (TextUtils.isEmpty(blockData) || !checkHexValue(blockData) || blockData.length() != 32) {
                showToast("blockData should be 32 hex characters!");
                data.requestFocus();
                return false;
            }
        }
        if (key2 != null) {
            String blockKey = key2.getText().toString();
            if (TextUtils.isEmpty(blockKey) || !checkHexValue(blockKey) || blockKey.length() != 32) {
                showToast("blockKey should be 32 hex characters!");
                key2.requestFocus();
                return false;
            }
        }
        return true;
    }

    /** Check hex string */
    private boolean checkHexValue(String src) {
        return Pattern.matches("[0-9a-fA-F]+", src);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelCheckCard();
    }

    private void cancelCheckCard() {
        try {
            MyApplication.mReadCardOptV2.cardOff(AidlConstantsV2.CardType.MIFARE_PLUS.getValue());
            MyApplication.mReadCardOptV2.cancelCheckCard();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
