package com.sm.sdk.demo.card;

import android.os.Bundle;
import android.os.RemoteException;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.Constant;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.card.wrapper.CheckCardCallbackV2Wrapper;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sunmi.pay.hardware.aidlv2.AidlConstantsV2;
import com.sunmi.pay.hardware.aidlv2.AidlErrorCodeV2;
import com.sunmi.pay.hardware.aidlv2.readcard.CheckCardCallbackV2;

import java.util.Arrays;
import java.util.Locale;

public class M1Activity extends BaseAppCompatActivity {

    private EditText mEditSector1;
    private EditText mEditKeyA1;
    private EditText mEditKeyB1;
    private EditText mEditBlock0;
    private EditText mEditBlock1;
    private EditText mEditBlock2;

    private EditText mEditSector2;
    private EditText mEditBlock;
    private EditText mEditKeyA2;
    private EditText mEditKeyB2;
    private EditText mEditCost;

    private EditText mEditSector3;
    private EditText mEditBlock3;
    private EditText mEditKeyA3;
    private EditText mEditKeyB3;

    private TextView mTvBalance;

    private int block;
    private int sector;
    private int keyType;    // 密钥类型，0表示KEY A、1表示 KEY B
    private byte[] keyBytes;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_m1);
        initView();
        checkCard();
    }

    private void initView() {
        initToolbarBringBack(R.string.card_test_m1);

        mEditSector1 = findViewById(R.id.edit_sector_1);
        mEditKeyA1 = findViewById(R.id.edit_keyA_1);
        mEditKeyB1 = findViewById(R.id.edit_keyB_1);
        mEditBlock0 = findViewById(R.id.edit_block_0);
        mEditBlock1 = findViewById(R.id.edit_block_1);
        mEditBlock2 = findViewById(R.id.edit_block_2);

        mEditSector2 = findViewById(R.id.edit_sector_2);
        mEditKeyA2 = findViewById(R.id.edit_keyA_2);
        mEditKeyB2 = findViewById(R.id.edit_keyB_2);
        mEditBlock = findViewById(R.id.edit_block);
        mEditCost = findViewById(R.id.edit_cost);

        mEditSector3 = findViewById(R.id.edit_sector_3);
        mEditBlock3 = findViewById(R.id.edit_block_3);
        mEditKeyA3 = findViewById(R.id.edit_keyA_3);
        mEditKeyB3 = findViewById(R.id.edit_keyB_3);

        mTvBalance = findViewById(R.id.tv_balance);

        findViewById(R.id.mb_read).setOnClickListener(this);
        findViewById(R.id.mb_write).setOnClickListener(this);
        findViewById(R.id.mb_init).setOnClickListener(this);
        findViewById(R.id.mb_balance).setOnClickListener(this);
        findViewById(R.id.mb_add).setOnClickListener(this);
        findViewById(R.id.mb_reduce).setOnClickListener(this);
        findViewById(R.id.mb_restore).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.mb_read:
//                readEntireCard();
                boolean check = checkParams();
                if (check) {
                    readAllSector();
                }
                break;
            case R.id.mb_write:
//                writeEntireCard();
                check = checkParams();
                if (check) {
                    writeAllSector();
                }
                break;
            case R.id.mb_init:
                check = checkParamsWallet();
                if (check) {
                    initWallet();
                }
                break;
            case R.id.mb_balance:
                check = checkParamsWallet();
                if (check) {
                    getBalanceWallet();
                }
                break;
            case R.id.mb_add:
                check = checkParamsWallet();
                if (check) {
                    increaseValueWallet();
                }
                break;
            case R.id.mb_reduce:
                check = checkParamsWallet();
                if (check) {
                    decreaseValueWallet();
                }
                break;
            case R.id.mb_restore:
                check = checkParamsRestore();
                if (check) {
                    restore();
                }
                break;
        }
    }

    private void checkCard() {
        try {
            showSwingCardHintDialog();
            MyApplication.mReadCardOptV2.checkCard(AidlConstantsV2.CardType.MIFARE.getValue(), mCheckCardCallback, 60);
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

    private void readAllSector() {
        int startBlockNo = sector * 4;
        boolean result = m1Auth(keyType, startBlockNo, keyBytes);
        if (result) {
            byte[] outData = new byte[128];
            int res = m1ReadBlock(startBlockNo, outData);
            if (res >= 0 && res <= 16) {
                String hexStr = ByteUtil.bytes2HexStr(Arrays.copyOf(outData, res));
                LogUtil.e(Constant.TAG, "read outData:" + hexStr);
                mEditBlock0.setText(hexStr);
            } else {
                mEditBlock0.setText(R.string.fail);
            }

            outData = new byte[128];
            res = m1ReadBlock(startBlockNo + 1, outData);
            if (res >= 0 && res <= 16) {
                String hexStr = ByteUtil.bytes2HexStr(Arrays.copyOf(outData, res));
                LogUtil.e(Constant.TAG, "read outData:" + hexStr);
                mEditBlock1.setText(hexStr);
            } else {
                mEditBlock1.setText(R.string.fail);
            }

            outData = new byte[128];
            res = m1ReadBlock(startBlockNo + 2, outData);
            if (res >= 0 && res <= 16) {
                String hexStr = ByteUtil.bytes2HexStr(Arrays.copyOf(outData, res));
                LogUtil.e(Constant.TAG, "read outData:" + hexStr);
                mEditBlock2.setText(hexStr);
            } else {
                mEditBlock2.setText(R.string.fail);
            }
        }
    }

    private void writeAllSector() {
        int startBlockNo = sector * 4;
        boolean result = m1Auth(keyType, startBlockNo, keyBytes);
        if (result) {
            String val = mEditBlock0.getText().toString();
            if (val.length() == 32) {
                byte[] inData = ByteUtil.hexStr2Bytes(val);
                int res = m1WriteBlock(startBlockNo, inData);
                if (res == 0) {
                    mEditBlock0.setText("");
                } else {
                    mEditBlock0.setText(R.string.fail);
                }
            }

            val = mEditBlock1.getText().toString();
            if (val.length() == 32) {
                byte[] inData = ByteUtil.hexStr2Bytes(val);
                int res = m1WriteBlock(startBlockNo + 1, inData);
                if (res == 0) {
                    mEditBlock1.setText("");
                } else {
                    mEditBlock1.setText(R.string.fail);
                }
            }

            val = mEditBlock2.getText().toString();
            if (val.length() == 32) {
                byte[] inData = ByteUtil.hexStr2Bytes(val);
                int res = m1WriteBlock(startBlockNo + 2, inData);
                if (res == 0) {
                    mEditBlock2.setText("");
                } else {
                    mEditBlock2.setText(R.string.fail);
                }
            }
        }
    }

    /**
     * init wallet format
     */
    private void initWallet() {
        boolean result = m1Auth(keyType, block, keyBytes);
        if (result) {
            byte[] inData = getInitFormatData(block);
            String hexStr = ByteUtil.bytes2HexStr(inData);
            LogUtil.e(Constant.TAG, "init wallet format inData:" + hexStr);
            int res = m1WriteBlock(block, inData);
            if (res == 0) {
                showToast(R.string.card_wallet_init_success);
                getBalanceWallet();
            } else {
                String error = getString(R.string.card_wallet_init_fail) + ":" + res;
                showToast(error);
            }
        }
    }

    /**
     * get wallet balance
     */
    private void getBalanceWallet() {
        boolean result = m1Auth(keyType, block, keyBytes);
        if (result) {
            byte[] outData = new byte[128];
            int res = m1ReadBlock(block, outData);
            if (res >= 0 && res <= 16) {
                String hexStr = ByteUtil.bytes2HexStr(Arrays.copyOf(outData, res));
                LogUtil.e(Constant.TAG, "get wallet balance outData:" + hexStr);
                int balance = ByteUtil.unsignedInt2IntLE(outData, 0);
                String val = getString(R.string.card_balance_symbol) + balance;
                mTvBalance.setText(val);
            } else {
                String error = getString(R.string.card_wallet_balance_fail) + ":" + res;
                showToast(error);
            }
        }
    }

    /**
     * increase wallet value
     */
    private void increaseValueWallet() {
        String costStr = mEditCost.getText().toString();
        int amount;
        try {
            amount = Integer.parseInt(costStr);
        } catch (Exception e) {
            e.printStackTrace();
            showToast(R.string.card_cost_hint);
            return;
        }
        boolean result = m1Auth(keyType, block, keyBytes);
        if (result) {
            byte[] inData = ByteUtil.int2BytesLE(amount);
            int res = m1IncValue(block, inData);
            if (res == 0) {
                // showToast(R.string.card_wallet_add_value_success);
                getBalanceWallet();
            } else {
                String error = getString(R.string.card_wallet_add_value_fail) + ":" + res;
                showToast(error);
            }
        }
    }

    /**
     * decrease wallet value
     */
    private void decreaseValueWallet() {
        String costStr = mEditCost.getText().toString();
        int amount;
        try {
            amount = Integer.parseInt(costStr);
        } catch (Exception e) {
            e.printStackTrace();
            showToast(R.string.card_cost_hint);
            return;
        }
        boolean result = m1Auth(keyType, block, keyBytes);
        if (result) {
            byte[] inData = ByteUtil.int2BytesLE(amount);
            int res = m1DecValue(block, inData);
            if (res == 0) {
                // showToast(R.string.card_wallet_dec_value_success);
                getBalanceWallet();
            } else {
                String error = getString(R.string.card_wallet_dec_value_fail) + ":" + res;
                showToast(error);
            }
        }
    }

    /** Mifare restore */
    private void restore() {
        try {
            boolean result = m1Auth(keyType, block, keyBytes);
            if (!result) {
                return;
            }
            int code = MyApplication.mReadCardOptV2.mifareRestore(block);
            showToast(code == 0 ? "success" : "fail");

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private boolean checkParams() {
        String sectorStr = mEditSector1.getText().toString();
        String keyAStr = mEditKeyA1.getText().toString();
        String keyBStr = mEditKeyB1.getText().toString();
        try {
            sector = Integer.parseInt(sectorStr);
        } catch (Exception e) {
            e.printStackTrace();
            showToast(R.string.card_sector_hint);
            return false;
        }
        if (keyAStr.length() == 12) {
            keyType = 0;
            keyBytes = ByteUtil.hexStr2Bytes(keyAStr);
        }
        if (keyBStr.length() == 12) {
            keyType = 1;
            keyBytes = ByteUtil.hexStr2Bytes(keyBStr);
        }
        if (keyBytes == null) {
            showToast(R.string.card_key_hint);
            return false;
        }

        return true;
    }

    private boolean checkParamsWallet() {
        String sectorStr = mEditSector2.getText().toString();
        String blockStr = mEditBlock.getText().toString();
        String keyAStr = mEditKeyA2.getText().toString();
        String keyBStr = mEditKeyB2.getText().toString();
        try {
            sector = Integer.parseInt(sectorStr);
        } catch (Exception e) {
            e.printStackTrace();
            showToast(R.string.card_sector_hint);
            return false;
        }
        try {
            block = Integer.parseInt(blockStr);
        } catch (Exception e) {
            e.printStackTrace();
            showToast(R.string.card_block_hint);
            return false;
        }
        if (keyAStr.length() == 12) {
            keyType = 0;
            keyBytes = ByteUtil.hexStr2Bytes(keyAStr);
        }
        if (keyBStr.length() == 12) {
            keyType = 1;
            keyBytes = ByteUtil.hexStr2Bytes(keyBStr);
        }
        if (keyBytes == null) {
            showToast(R.string.card_key_hint);
            return false;
        }

        // calculate block
        block = sector * 4 + block;

        return true;
    }

    private boolean checkParamsRestore() {
        String sectorStr = mEditSector3.getText().toString();
        String blockStr = mEditBlock3.getText().toString();
        String keyAStr = mEditKeyA3.getText().toString();
        String keyBStr = mEditKeyB3.getText().toString();
        try {
            sector = Integer.parseInt(sectorStr);
        } catch (Exception e) {
            e.printStackTrace();
            showToast(R.string.card_sector_hint);
            return false;
        }
        try {
            block = Integer.parseInt(blockStr);
        } catch (Exception e) {
            e.printStackTrace();
            showToast(R.string.card_block_hint);
            return false;
        }
        if (keyAStr.length() == 12) {
            keyType = 0;
            keyBytes = ByteUtil.hexStr2Bytes(keyAStr);
        }
        if (keyBStr.length() == 12) {
            keyType = 1;
            keyBytes = ByteUtil.hexStr2Bytes(keyBStr);
        }
        if (keyBytes == null) {
            showToast(R.string.card_key_hint);
            return false;
        }

        // calculate block
        block = sector * 4 + block;
        return true;
    }

    /**
     * init wallet format data
     */
    private byte[] getInitFormatData(int blockIndex) {
        byte[] result = {
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
        };
        result[12] = (byte) (blockIndex & 0xFF);
        result[13] = (byte) ~(blockIndex & 0xFF);
        result[14] = (byte) (blockIndex & 0xFF);
        result[15] = (byte) ~(blockIndex & 0xFF);
        return result;
    }

    /**
     * m1 card auth
     */
    private boolean m1Auth(int keyType, int block, byte[] keyData) {
        int result = -1;
        try {
            String hexStr = ByteUtil.bytes2HexStr(keyData);
            LogUtil.e(Constant.TAG, "block:" + block + " keyType:" + keyType + " keyBytes:" + hexStr);
            result = MyApplication.mReadCardOptV2.mifareAuth(keyType, block, keyData);
            LogUtil.e(Constant.TAG, "m1Auth result:" + result);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (result == 0) {
            return true;
        }
        String msg = String.format(Locale.getDefault(), "%s:%d(%s)", getString(R.string.card_auth_fail),
                result, AidlErrorCodeV2.valueOf(result).getMsg());
        showToast(msg);
        checkCard();
        return false;
    }

    /**
     * m1 write block data
     */
    private int m1WriteBlock(int block, byte[] blockData) {
        try {
            int result = MyApplication.mReadCardOptV2.mifareWriteBlock(block, blockData);
            LogUtil.e(Constant.TAG, "m1WriteBlock result:" + result);
            return result;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -123;
    }

    /**
     * m1 read block data
     */
    private int m1ReadBlock(int block, byte[] blockData) {
        try {
            int result = MyApplication.mReadCardOptV2.mifareReadBlock(block, blockData);
            LogUtil.e(Constant.TAG, "m1ReadBlock result:" + result);
            return result;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -123;
    }

    /**
     * m1 increase value
     */
    private int m1IncValue(int block, byte[] blockData) {
        try {
            int result = MyApplication.mReadCardOptV2.mifareIncValue(block, blockData);
            LogUtil.e(Constant.TAG, "m1IncValue result:" + result);
            return result;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -123;
    }

    /**
     * m1 decrease value
     */
    private int m1DecValue(int block, byte[] blockData) {
        try {
            int result = MyApplication.mReadCardOptV2.mifareDecValue(block, blockData);
            LogUtil.e(Constant.TAG, "m1DecValue result:" + result);
            return result;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -123;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelCheckCard();
    }

    private void cancelCheckCard() {
        try {
            MyApplication.mReadCardOptV2.cardOff(AidlConstantsV2.CardType.MIFARE.getValue());
            MyApplication.mReadCardOptV2.cancelCheckCard();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private byte[] readEntireCard() {
        byte[] key = {(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
        byte[] cardData = new byte[0];
        long startTime = System.currentTimeMillis();
        for (int sector = 0; sector < 16; sector++) {
            int auth = authToSector(sector, key);
            if (auth != 0) {
                LogUtil.e(Constant.TAG, "auth failed to sector " + sector + " : " + auth);
                return null;
            }
            for (int block = 0; block < 4; block++) {
                byte[] data = new byte[16];
                int bytesRead = readBlock(sector * 4 + block, data);
                if (bytesRead != 16) {
                    LogUtil.e(Constant.TAG, "read failed to " + sector + "-" + block + " : " + bytesRead);
                    return null;
                } else {
                    cardData = Arrays.copyOf(cardData, cardData.length + bytesRead);
                    System.arraycopy(data, 0, cardData, cardData.length - bytesRead, bytesRead);
                }
            }
        }
        long endTime = System.currentTimeMillis();
        LogUtil.e(Constant.TAG, "readEntireCard time:" + (endTime - startTime));
        return cardData;
    }

    private void writeEntireCard() {
        byte[] key = {(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
        byte[] writeBlock = new byte[16];
        Arrays.fill(writeBlock, (byte) 0x00);
        long startTime = System.currentTimeMillis();
        for (int sector = 0; sector < 16; sector++) {
            int auth = authToSector(sector, key);
            if (auth != 0) {
                LogUtil.e(Constant.TAG, "auth failed to sector " + sector + " : " + auth);
                return;
            }

            for (int block = 0; block < 3; block++) {
                if (sector != 0 || block != 0) {
                    int write = writeBlock(sector * 4 + block, writeBlock);
                    if (write != 0) {
                        LogUtil.e(Constant.TAG, "write failed to " + sector + "-" + block + " : " + write);
                        return;
                    }
                }
            }
        }
        long endTime = System.currentTimeMillis();
        LogUtil.e(Constant.TAG, "writeEntireCard time:" + (endTime - startTime));
    }

    private int authToSector(int sector, byte[] key) {
        try {
            return MyApplication.mReadCardOptV2.mifareAuth(0, sector * 4, key);
        } catch (RemoteException re) {
            re.printStackTrace();
            return -1;
        }
    }

    private int readBlock(int block, byte[] data) {
        try {
            return MyApplication.mReadCardOptV2.mifareReadBlock(block, data);
        } catch (RemoteException re) {
            re.printStackTrace();
            return -1;
        }
    }

    private int writeBlock(int block, byte[] data) {
        try {
            return MyApplication.mReadCardOptV2.mifareWriteBlock(block, data);
        } catch (RemoteException re) {
            re.printStackTrace();
            return -1;
        }
    }
}
