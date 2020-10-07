package com.sm.sdk.demo.card;

import android.os.Bundle;
import android.os.RemoteException;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.Constant;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.card.wrapper.CheckCardCallbackV2Wrapper;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sunmi.pay.hardware.aidl.AidlConstants.CardType;
import com.sunmi.pay.hardware.aidlv2.readcard.CheckCardCallbackV2;

import java.util.Arrays;
import java.util.Locale;

/**
 * This page show how to Authenticate/Read/Write/Increment/Decrement
 * Mifare Classic by using the key stored in Mifare SAM AV2.
 * <br/>
 * At this moment, this page only used for testing, please do not refer to
 * any code in this page.
 */
public class MifareAuthedByPSAMActivity extends BaseAppCompatActivity {
    private static final String TAG = Constant.TAG;

    private String uuid;
    private TextView result;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mifare_psam_test);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.card_test_m1_psame);
        findViewById(R.id.check_psam).setOnClickListener(this);
        findViewById(R.id.check_mifare).setOnClickListener(this);
        findViewById(R.id.m1_auth).setOnClickListener(this);
        findViewById(R.id.m1_read).setOnClickListener(this);
        findViewById(R.id.m1_write).setOnClickListener(this);
        findViewById(R.id.m1_inc).setOnClickListener(this);
        findViewById(R.id.m1_dec).setOnClickListener(this);
        result = findViewById(R.id.result);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.check_psam:
                checkCardPsam();
                break;
            case R.id.check_mifare:
                checkCardMifare();
                break;
            case R.id.m1_auth:
                mifareAuth();
                break;
            case R.id.m1_read:
                mifareReadBlock();
                break;
            case R.id.m1_write:
                mifareWriteBlock();
                break;
            case R.id.m1_inc:
                mifareIncDecValue(0);
                break;
            case R.id.m1_dec:
                mifareIncDecValue(1);
                break;
        }
    }

    private void checkCardPsam() {
        try {
            result.setText(null);
            MyApplication.mReadCardOptV2.checkCard(CardType.PSAM0.getValue(), checkCardCallbackV2, 60);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void checkCardMifare() {
        try {
            result.setText(null);
            MyApplication.mReadCardOptV2.checkCard(CardType.MIFARE.getValue(), checkCardCallbackV2, 60);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private CheckCardCallbackV2 checkCardCallbackV2 = new CheckCardCallbackV2Wrapper() {

        @Override
        public void findMagCard(Bundle bundle) throws RemoteException {
            LogUtil.e(TAG, "findMagCard,bundle:" + bundle);
        }

        @Override
        public void findICCard(String atr) throws RemoteException {
            LogUtil.e(TAG, "findICCard,atr:" + atr);
        }

        @Override
        public void findRFCard(String puuid) throws RemoteException {
            LogUtil.e(TAG, "findRFCard,uuid:" + uuid);
            uuid = puuid;
        }

        @Override
        public void onError(int code, String msg) throws RemoteException {
            LogUtil.e(TAG, "onError,code:" + code + ",msg:" + msg);
            showToast(msg);
        }
    };

    /** PSAM-Mifare authentication */
    private void mifareAuth() {
        try {
// 第1字节含义：
//（1）低4位(rxCRC-应答crc校验，rx-parity应答奇偶校验；txCRC-发送CRC校验，tx-parity发送奇偶校验)
//     bit1=0 disable rx crc, bit1=1 enable  rx crc,
//     bit2=0 disable tx crc, bit2=1 enable  tx crc
//     bit3=0 enable  rx parity, bit3=1 disable rx parity
//     bit4=0 enable  tx parity, bit4=1 disable tx parity
//（2）高4位
//     第一个字节高4位设置TxLastBits
//     高4位一般是0，代表发送最后一个字节的全部8bit数据
//     如果不为0那么代表发送最后一个字节的若干位bit

            result.setText(null);
            //step1: Send to MIFARE Classic
            byte[] send = {0x02, 0x60, 0x10};
            byte[] out = new byte[128];
            int len = MyApplication.mReadCardOptV2.transmitApduEx(CardType.MIFARE.getValue(), send, out);
            if (len < 0) {
                String msg = "apdu交互出错，code:" + len;
                showResult(1, msg);
                LogUtil.e(TAG, msg);
                return;
            }
            //step2: Response from MIFARE Plus
            String m1Rsp1 = ByteUtil.bytes2HexStr(Arrays.copyOf(out, len));
            LogUtil.e(TAG, "step2 Response from MIFARE:" + m1Rsp1);
            showResult(2, m1Rsp1);

            //step3: First part of SAM_AuthenticateMIFARE
            String tuuid = uuid;
            if (tuuid.length() > 8) {
                tuuid = tuuid.substring(tuuid.length() - 8);
            }
            String hexSend = "801C00000D" + tuuid + "08010A10" + m1Rsp1 + "0000";
            send = ByteUtil.hexStr2Bytes(hexSend);
            len = MyApplication.mReadCardOptV2.transmitApduEx(CardType.PSAM0.getValue(), send, out);
            if (len < 0) {
                String msg = "apdu交互出错，code:" + len;
                showResult(3, msg);
                LogUtil.e(TAG, msg);
                return;
            }
            //step4: Response from SAM, R-APDU
            String psamRsp1 = ByteUtil.bytes2HexStr(Arrays.copyOf(out, len));
            LogUtil.e(TAG, "step4 Response from SAM, R-APDU :" + psamRsp1);
            showResult(4, psamRsp1);

            //step5: Send the SAM response to MIFARE Plus
            hexSend = "0C" + psamRsp1.substring(0, psamRsp1.length() - 4);//去掉末尾的90xx字节
            send = ByteUtil.hexStr2Bytes(hexSend);
            len = MyApplication.mReadCardOptV2.transmitApduEx(CardType.MIFARE.getValue(), send, out);
            if (len < 0) {
                String msg = "apdu交互出错，code:" + len;
                showResult(5, msg);
                LogUtil.e(TAG, msg);
                return;
            }
            //step6: Response from MIFARE Plus
            String m1Rsp2 = ByteUtil.bytes2HexStr(Arrays.copyOf(out, len));
            LogUtil.e(TAG, "step6 Response from MIFARE:" + m1Rsp2);
            showResult(6, m1Rsp2);

            //step7: 2nd part of SAM_AuthenticateMIFARE
            String hexLen = String.format("%02X", m1Rsp2.length() / 2);
            hexSend = "801C0000" + hexLen + m1Rsp2;
            send = ByteUtil.hexStr2Bytes(hexSend);
            len = MyApplication.mReadCardOptV2.transmitApduEx(CardType.PSAM0.getValue(), send, out);
            if (len < 0) {
                String msg = "apdu交互出错，code:" + len;
                showResult(4, msg);
                LogUtil.e(TAG, msg);
                return;
            }
            //step8: R-APDU
            String psamRsp2 = ByteUtil.bytes2HexStr(Arrays.copyOf(out, len));
            LogUtil.e(TAG, "step8 R-APDU:" + psamRsp2);
            showResult(4, psamRsp2);

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** PSAM-Mifare read block data */
    private void mifareReadBlock() {
// 第1字节含义：
//（1）低4位(rxCRC-应答crc校验，rx-parity应答奇偶校验；txCRC-发送CRC校验，tx-parity发送奇偶校验)
//     bit1=0 disable rx crc, bit1=1 enable  rx crc,
//     bit2=0 disable tx crc, bit2=1 enable  tx crc
//     bit3=0 enable  rx parity, bit3=1 disable rx parity
//     bit4=0 enable  tx parity, bit4=1 disable tx parity
//（2）高4位
//     第一个字节高4位设置TxLastBits
//     高4位一般是0，代表发送最后一个字节的全部8bit数据
//     如果不为0那么代表发送最后一个字节的若干位bit
        try {
            result.setText(null);
            //step1: Send to MIFARE SAM AV2
            String hexSend = "80ED000002301000";//读命令：0x03，读取的块号:0x10（第16块，块号从0开始）
            byte[] send = ByteUtil.hexStr2Bytes(hexSend);
            byte[] out = new byte[128];
            int len = MyApplication.mReadCardOptV2.transmitApduEx(CardType.PSAM0.getValue(), send, out);
            if (len < 0) {
                String msg = "apdu交互出错，code:" + len;
                showResult(1, msg);
                LogUtil.e(TAG, msg);
                return;
            }
            //step2: Received from MIFARE SAM AV2
            String psamRsp1 = ByteUtil.bytes2HexStr(Arrays.copyOf(out, len));
            LogUtil.e(TAG, "step2 Received from MIFARE SAM AV2:" + psamRsp1);
            showResult(2, psamRsp1);

            //step3: Send to MIFARE Classic
            hexSend = "4C" + psamRsp1.substring(0, psamRsp1.length() - 4);//去掉末尾的90xx字节
            send = ByteUtil.hexStr2Bytes(hexSend);
            len = MyApplication.mReadCardOptV2.transmitApduEx(CardType.MIFARE.getValue(), send, out);
            if (len < 0) {
                String msg = "apdu交互出错，code:" + len;
                showResult(3, msg);
                LogUtil.e(TAG, msg);
                return;
            }
            //step4: Received from MIFARE Classic(Encrypted data from MIFARE Classic.)
            String m1Rsp1 = ByteUtil.bytes2HexStr(Arrays.copyOf(out, len));
            LogUtil.e(TAG, "step4 Received from MIFARE Classic:" + m1Rsp1);
            showResult(4, m1Rsp1);

            //step5: Send the encrypted data to SAM AV2 for decryption
            String hexLen = String.format("%02X", m1Rsp1.length() / 2);
            hexSend = "80DD0000" + hexLen + m1Rsp1 + "00";
            send = ByteUtil.hexStr2Bytes(hexSend);
            len = MyApplication.mReadCardOptV2.transmitApduEx(CardType.PSAM0.getValue(), send, out);
            if (len < 0) {
                String msg = "apdu交互出错，code:" + len;
                showResult(5, msg);
                LogUtil.e(TAG, msg);
                return;
            }
            //step6: Plain data received from SAM AV2
            String psamRsp2 = ByteUtil.bytes2HexStr(Arrays.copyOf(out, len));
            LogUtil.e(TAG, "step6 Plain data received from SAM AV2:" + psamRsp2);
            showResult(6, psamRsp2);

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** PSAM-Mifare write block data */
    private void mifareWriteBlock() {
// 第1字节含义：
//（1）低4位(rxCRC-应答crc校验，rx-parity应答奇偶校验；txCRC-发送CRC校验，tx-parity发送奇偶校验)
//     bit1=0 disable rx crc, bit1=1 enable  rx crc,
//     bit2=0 disable tx crc, bit2=1 enable  tx crc
//     bit3=0 enable  rx parity, bit3=1 disable rx parity
//     bit4=0 enable  tx parity, bit4=1 disable tx parity
//（2）高4位
//     第一个字节高4位设置TxLastBits
//     高4位一般是0，代表发送最后一个字节的全部8bit数据
//     如果不为0那么代表发送最后一个字节的若干位bit
        try {
            result.setText(null);
            //step1: Send write command to SAM for encryption
            String hexSend = "80ED000002A01100";//写命令：0xA0，写的块号:0x10（第16块，块号从0开始）
            byte[] send = ByteUtil.hexStr2Bytes(hexSend);
            byte[] out = new byte[128];
            int len = MyApplication.mReadCardOptV2.transmitApduEx(CardType.PSAM0.getValue(), send, out);
            if (len < 0) {
                String msg = "apdu交互出错，code:" + len;
                showResult(1, msg);
                LogUtil.e(TAG, msg);
                return;
            }
            //step2: SAM returns encrypted write command
            String psamRsp1 = ByteUtil.bytes2HexStr(Arrays.copyOf(out, len));
            LogUtil.e(TAG, "step2 SAM returns encrypted write command:" + psamRsp1);
            showResult(2, psamRsp1);

            //step3: Send to MIFARE Classic
            hexSend = "4C" + psamRsp1.substring(0, psamRsp1.length() - 4);//去掉末尾的90xx字节
            send = ByteUtil.hexStr2Bytes(hexSend);
            len = MyApplication.mReadCardOptV2.transmitApduEx(CardType.MIFARE.getValue(), send, out);
            if (len < 0) {
                String msg = "apdu交互出错，code:" + len;
                showResult(3, msg);
                LogUtil.e(TAG, msg);
                return;
            }
            //step4: MIFARE classic returns ACK(Encrypted ACK)
            String m1Rsp1 = ByteUtil.bytes2HexStr(Arrays.copyOf(out, len));
            LogUtil.e(TAG, "step4 MIFARE classic returns ACK(Encrypted ACK):" + m1Rsp1);
            showResult(4, m1Rsp1);

            //step5: Encrypted ACK is sent to SAM for decryption
            String hexLen = String.format("%02X", m1Rsp1.length() / 2);
            hexSend = "80DD0000" + hexLen + m1Rsp1 + "00";
            send = ByteUtil.hexStr2Bytes(hexSend);
            len = MyApplication.mReadCardOptV2.transmitApduEx(CardType.PSAM0.getValue(), send, out);
            if (len < 0) {
                String msg = "apdu交互出错，code:" + len;
                showResult(5, msg);
                LogUtil.e(TAG, msg);
                return;
            }
            //step6: ACK(Plain ACK from SAM)
            String psamRsp2 = ByteUtil.bytes2HexStr(Arrays.copyOf(out, len));
            LogUtil.e(TAG, "step6 ACK(Plain ACK from SAM):" + psamRsp2);
            showResult(6, psamRsp2);

            //step7: Data to be written is sent to SAM for encryption
            String data = "00112233445566778899AABBCCDDEEFF";
            hexLen = String.format("%02X", data.length() / 2);
            hexSend = "80ED0000" + hexLen + data + "00";
            send = ByteUtil.hexStr2Bytes(hexSend);
            len = MyApplication.mReadCardOptV2.transmitApduEx(CardType.PSAM0.getValue(), send, out);
            if (len < 0) {
                String msg = "apdu交互出错，code:" + len;
                showResult(7, msg);
                LogUtil.e(TAG, msg);
                return;
            }
            //step8: Encrypted data from SAM
            String psamRsp3 = ByteUtil.bytes2HexStr(Arrays.copyOf(out, len));
            LogUtil.e(TAG, "step8 Encrypted data from SAM:" + psamRsp3);
            showResult(8, psamRsp3);

            //step9: Encrypted data is sent to MIFARE Classic
            hexSend = "2C" + psamRsp3.substring(0, psamRsp3.length() - 4);//去掉末尾的90xx字节
            send = ByteUtil.hexStr2Bytes(hexSend);
            len = MyApplication.mReadCardOptV2.transmitApduEx(CardType.MIFARE.getValue(), send, out);
            if (len < 0) {
                String msg = "apdu交互出错，code:" + len;
                showResult(9, msg);
                LogUtil.e(TAG, msg);
                return;
            }
            //step10: Encrypted ACK from MIFARE Classic
            String m1Rsp2 = ByteUtil.bytes2HexStr(Arrays.copyOf(out, len));
            LogUtil.e(TAG, "step10  Encrypted ACK from MIFARE Classic:" + m1Rsp2);
            showResult(10, m1Rsp2);


            //step11: Encrypted ACK is sent to SAM for decryption
            hexLen = String.format("%02X", m1Rsp2.length() / 2);
            hexSend = "80DD0000" + hexLen + m1Rsp1 + "00";
            send = ByteUtil.hexStr2Bytes(hexSend);
            len = MyApplication.mReadCardOptV2.transmitApduEx(CardType.PSAM0.getValue(), send, out);
            if (len < 0) {
                String msg = "apdu交互出错，code:" + len;
                showResult(11, msg);
                LogUtil.e(TAG, msg);
                return;
            }
            //step12: ACK(Plain ACK from SAM)
            String psamRsp4 = ByteUtil.bytes2HexStr(Arrays.copyOf(out, len));
            LogUtil.e(TAG, "step12 ACK(Plain ACK from SAM):" + psamRsp4);
            showResult(12, psamRsp4);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * PSAM-Mifare increment/decrement
     *
     * @param optType 0-increment, 1-decrement
     */
    private void mifareIncDecValue(int optType) {
// 第1字节含义：
//（1）低4位(rxCRC-应答crc校验，rx-parity应答奇偶校验；txCRC-发送CRC校验，tx-parity发送奇偶校验)
//     bit1=0 disable rx crc, bit1=1 enable  rx crc,
//     bit2=0 disable tx crc, bit2=1 enable  tx crc
//     bit3=0 enable  rx parity, bit3=1 disable rx parity
//     bit4=0 enable  tx parity, bit4=1 disable tx parity
//（2）高4位
//     第一个字节高4位设置TxLastBits
//     高4位一般是0，代表发送最后一个字节的全部8bit数据
//     如果不为0那么代表发送最后一个字节的若干位bit
        try {

//加值/减值包含两部分操作：
//    1.源值加/减值后存放在mifare芯片的内部缓存区
//    2.将缓存区的值转存到目标block
            result.setText(null);
            //Part1：加/减值
            String cmd = optType == 0 ? "C1" : "C0";//加值命令：0xC1，减值命令:0xC0
            //step1: Send inc/dec command to SAM for encryption
            String hexSend = "80ED000002" + cmd + "1000";//操作的块号:0x10（第16块，块号从0开始）
            byte[] send = ByteUtil.hexStr2Bytes(hexSend);
            byte[] out = new byte[128];
            int len = MyApplication.mReadCardOptV2.transmitApduEx(CardType.PSAM0.getValue(), send, out);
            if (len < 0) {
                String msg = "apdu交互出错，code:" + len;
                showResult(1, msg);
                LogUtil.e(TAG, msg);
                return;
            }
            //step2: SAM returns encrypted inc/dec command
            String psamRsp1 = ByteUtil.bytes2HexStr(Arrays.copyOf(out, len));
            LogUtil.e(TAG, "step2 SAM returns encrypted inc/dec command:" + psamRsp1);
            showResult(2, psamRsp1);

            //step3: Send to MIFARE Classic
            hexSend = "4C" + psamRsp1.substring(0, psamRsp1.length() - 4);//去掉末尾的90xx字节
            send = ByteUtil.hexStr2Bytes(hexSend);
            len = MyApplication.mReadCardOptV2.transmitApduEx(CardType.MIFARE.getValue(), send, out);
            if (len < 0) {
                String msg = "apdu交互出错，code:" + len;
                showResult(3, msg);
                LogUtil.e(TAG, msg);
                return;
            }
            //step4: MIFARE classic returns ACK(Encrypted ACK)
            String m1Rsp1 = ByteUtil.bytes2HexStr(Arrays.copyOf(out, len));
            LogUtil.e(TAG, "step4 MIFARE classic returns ACK(Encrypted ACK):" + m1Rsp1);
            showResult(4, m1Rsp1);

            //step5: Encrypted ACK is sent to SAM for decryption
            String hexLen = String.format("%02X", m1Rsp1.length() / 2);
            hexSend = "80DD0000" + hexLen + m1Rsp1 + "00";
            send = ByteUtil.hexStr2Bytes(hexSend);
            len = MyApplication.mReadCardOptV2.transmitApduEx(CardType.PSAM0.getValue(), send, out);
            if (len < 0) {
                String msg = "apdu交互出错，code:" + len;
                showResult(5, msg);
                LogUtil.e(TAG, msg);
                return;
            }
            //step6: ACK(Plain ACK from SAM)
            String psamRsp2 = ByteUtil.bytes2HexStr(Arrays.copyOf(out, len));
            LogUtil.e(TAG, "step6 ACK(Plain ACK from SAM):" + psamRsp2);
            showResult(6, psamRsp2);

            //step7: Data to be inc/dec is sent to SAM for encryption
            String data = "00000001";//减值/减值的值为int，长度4Byte
            hexLen = String.format("%02X", data.length() / 2);
            hexSend = "80ED0000" + hexLen + data + "00";
            send = ByteUtil.hexStr2Bytes(hexSend);
            len = MyApplication.mReadCardOptV2.transmitApduEx(CardType.PSAM0.getValue(), send, out);
            if (len < 0) {
                String msg = "apdu交互出错，code:" + len;
                showResult(7, msg);
                LogUtil.e(TAG, msg);
                return;
            }
            //step8: Encrypted data from SAM
            String psamRsp3 = ByteUtil.bytes2HexStr(Arrays.copyOf(out, len));
            LogUtil.e(TAG, "step8 Encrypted data from SAM:" + psamRsp3);
            showResult(8, psamRsp3);

            //step9: Encrypted data is sent to MIFARE Classic
            hexSend = "6C" + psamRsp3.substring(0, psamRsp3.length() - 4);//去掉末尾的90xx字节
            send = ByteUtil.hexStr2Bytes(hexSend);
            len = MyApplication.mReadCardOptV2.transmitApduEx(CardType.MIFARE.getValue(), send, out);
            if (len < 0) {
                String msg = "apdu交互出错，code:" + len;
                showResult(9, msg);
                LogUtil.e(TAG, msg);
                return;
            }
            //step10: No Response
            String m1Rsp2 = ByteUtil.bytes2HexStr(Arrays.copyOf(out, len));
            LogUtil.e(TAG, "step10 No Response:" + m1Rsp2);
            showResult(10, m1Rsp2);

            //Part2：转存，把对16块的加/减值结果仍转存到16块
            //step11: Preparing transfer command data
            hexSend = "80DD000002B01000";//转存命令：0xB0，操作的块号:0x10（第16块，块号从0开始）
            send = ByteUtil.hexStr2Bytes(hexSend);
            len = MyApplication.mReadCardOptV2.transmitApduEx(CardType.PSAM0.getValue(), send, out);
            if (len < 0) {
                String msg = "apdu交互出错，code:" + len;
                showResult(11, msg);
                LogUtil.e(TAG, msg);
                return;
            }
            //step12: SAM encrypted transferred command
            String psamRsp4 = ByteUtil.bytes2HexStr(Arrays.copyOf(out, len));
            LogUtil.e(TAG, "step12 SAM encrypted transferred command:" + psamRsp4);
            showResult(12, psamRsp4);

            //step13: Send to MIFARE Classic
            hexSend = "4C" + psamRsp4.substring(0, psamRsp4.length() - 4);//去掉末尾的90xx字节
            send = ByteUtil.hexStr2Bytes(hexSend);
            len = MyApplication.mReadCardOptV2.transmitApduEx(CardType.MIFARE.getValue(), send, out);
            if (len < 0) {
                String msg = "apdu交互出错，code:" + len;
                showResult(13, msg);
                LogUtil.e(TAG, msg);
                return;
            }
            //step14: MIFARE classic returns ACK(Encrypted ACK)
            String m1Rsp3 = ByteUtil.bytes2HexStr(Arrays.copyOf(out, len));
            LogUtil.e(TAG, "step14 MIFARE classic returns ACK(Encrypted ACK):" + m1Rsp3);
            showResult(14, m1Rsp3);

            //step15: Checking the response of MIFARE card from SAM
            hexLen = String.format("%02X", m1Rsp3.length() / 2);
            hexSend = "80DD0000" + hexLen + m1Rsp3 + "00";
            send = ByteUtil.hexStr2Bytes(hexSend);
            len = MyApplication.mReadCardOptV2.transmitApduEx(CardType.PSAM0.getValue(), send, out);
            if (len < 0) {
                String msg = "apdu交互出错，code:" + len;
                showResult(15, msg);
                LogUtil.e(TAG, msg);
                return;
            }
            //step16: ACK(Plain ACK from SAM)
            psamRsp3 = ByteUtil.bytes2HexStr(Arrays.copyOf(out, len));
            LogUtil.e(TAG, "step16 ACK(Plain ACK from SAM):" + psamRsp3);
            showResult(16, psamRsp2);

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void showResult(int step, String msg) {
        runOnUiThread(() -> {
            String preMsg = result.getText().toString();
            result.setText(String.format(Locale.getDefault(), "%s\n(%d)%s", preMsg, step, msg));
        });
    }
}
